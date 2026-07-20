#!/bin/bash
set -euo pipefail

# DD_API_KEY가 없는 환경(로컬 검증 등)에서는 설치를 건너뛴다. 배포를 막지 않기 위함.
DD_API_KEY="$(/opt/elasticbeanstalk/bin/get-config environment -k DD_API_KEY 2>/dev/null || true)"
if [[ -z "${DD_API_KEY}" ]]; then
  echo "DD_API_KEY not set; skipping Datadog agent setup"
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
cat > /etc/datadog-agent/datadog.yaml <<EOF
api_key: ${DD_API_KEY}
site: ${DD_SITE}
env: ${DD_ENV}
logs_enabled: true
apm_config:
  enabled: true
  receiver_port: 8126
dogstatsd_port: 8125
EOF

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
