#!/bin/bash
set -euo pipefail

DD_API_KEY="$(/opt/elasticbeanstalk/bin/get-config environment -k DD_API_KEY 2>/dev/null || true)"

# EB 환경 속성에는 Secrets Manager 참조를 넣을 수 없으므로 인스턴스에서 직접 조회한다.
# 시크릿 이름은 비밀이 아니라 EB 환경 속성으로 주입하고, 키 값은 이 인스턴스 밖으로 나가지 않는다.
if [[ -z "${DD_API_KEY}" ]]; then
  SECRET_ID="$(/opt/elasticbeanstalk/bin/get-config environment -k DD_API_KEY_SECRET_ID 2>/dev/null || true)"
  if [[ -n "${SECRET_ID}" ]]; then
    SECRET_REGION="$(/opt/elasticbeanstalk/bin/get-config environment -k AWS_REGION 2>/dev/null || echo ap-northeast-2)"
    SECRET_STRING="$(aws secretsmanager get-secret-value --secret-id "${SECRET_ID}" \
      --region "${SECRET_REGION}" --query SecretString --output text 2>/dev/null || true)"
    # 평문 시크릿이면 그대로, JSON이면 DD_API_KEY 필드만 쓴다.
    # JSON에 해당 필드가 없으면 빈 값으로 두어 잘못된 키로 Agent를 띄우지 않는다.
    DD_API_KEY="$(printf '%s' "${SECRET_STRING}" | python3 -c '
import json, sys
raw = sys.stdin.read().strip()
try:
    parsed = json.loads(raw)
except Exception:
    print(raw)
else:
    print(parsed.get("DD_API_KEY", "") if isinstance(parsed, dict) else raw)
' 2>/dev/null || true)"
  fi
fi

# 키를 못 구한 환경(로컬 검증 등)에서는 설치를 건너뛴다. 배포를 막지 않기 위함.
if [[ -z "${DD_API_KEY}" ]]; then
  echo "DD_API_KEY not available from EB environment or Secrets Manager; skipping Datadog agent setup"
  exit 0
fi

# 관측성 구성 실패가 배포나 오토스케일링을 막아서는 안 된다.
# 아래부터는 어떤 단계가 실패해도 경고만 남기고 정상 종료한다.
trap 'echo "WARNING: Datadog agent setup failed at line ${LINENO}; continuing deployment without it" >&2; exit 0' ERR

DD_SITE="$(/opt/elasticbeanstalk/bin/get-config environment -k DD_SITE 2>/dev/null || echo datadoghq.com)"
DD_ENV="$(/opt/elasticbeanstalk/bin/get-config environment -k DD_ENV 2>/dev/null || echo dev)"

if ! command -v datadog-agent >/dev/null 2>&1; then
  DD_API_KEY="${DD_API_KEY}" DD_SITE="${DD_SITE}" DD_INSTALL_ONLY=true \
    bash -c "$(curl -fsSL https://install.datadoghq.com/scripts/install_script_agent7.sh)"
fi

# 재배포 때마다 최신 설정으로 덮어쓴다 (멱등)
# t4g.micro(1GB)에서 JVM과 공존해야 하므로 수집기를 최소 구성으로 유지한다.
# process/container 수집을 끄면 process-agent가 아예 뜨지 않아 100MB 안팎을 아낀다.
cat > /etc/datadog-agent/datadog.yaml <<EOF
api_key: ${DD_API_KEY}
site: ${DD_SITE}
env: ${DD_ENV}
logs_enabled: true
apm_config:
  enabled: true
  receiver_port: 8126
dogstatsd_port: 8125
process_config:
  process_collection:
    enabled: false
  container_collection:
    enabled: false
inventories_configuration_enabled: false
EOF

# 메모리 상한을 건다. GOMEMLIMIT은 Go GC를 공격적으로 돌려 상한 근처에서 회수를 늘리고,
# MemoryMax는 초과 시 cgroup이 해당 유닛만 종료시킨다.
# 한계 상황에서 관측성 프로세스가 죽고 애플리케이션은 살아남게 하려는 의도다.
write_memory_limit() {
  mkdir -p "/etc/systemd/system/$1.service.d"
  cat > "/etc/systemd/system/$1.service.d/memory.conf" <<EOF
[Service]
Environment="GOMEMLIMIT=$2"
MemoryMax=$3
MemorySwapMax=0
EOF
}
write_memory_limit datadog-agent 120MiB 180M
write_memory_limit datadog-agent-trace 60MiB 100M
systemctl daemon-reload

mkdir -p /etc/datadog-agent/conf.d/jobdori.d
cat > /etc/datadog-agent/conf.d/jobdori.d/conf.yaml <<'EOF'
logs:
  - type: file
    path: /var/log/web.stdout.log
    service: jobdori-api
    source: java
    log_processing_rules:
      - type: multi_line
        name: log_start_with_date
        pattern: '\d{4}-\d{2}-\d{2}'
  - type: file
    path: /var/app/current/logs/access_log.*.log
    service: jobdori-api
    source: tomcat-access
EOF

# 앱 액세스 로그는 /var/app/current/logs에 webapp:webapp 소유로 쓰인다.
# prebuild 시점엔 해당 디렉토리가 아직 없을 수 있으므로 경로가 아니라 그룹 멤버십으로 권한을 확보한다.
# (/var/log/web.stdout.log은 0644 root:root라 별도 조치가 필요 없다.)
usermod -a -G webapp dd-agent || echo "WARNING: could not add dd-agent to webapp group" >&2

systemctl enable datadog-agent
# 그룹 멤버십은 프로세스 시작 시점에만 적용되므로 restart가 필요하다
systemctl restart datadog-agent
