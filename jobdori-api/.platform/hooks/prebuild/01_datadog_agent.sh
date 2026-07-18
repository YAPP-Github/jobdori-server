#!/bin/bash
set -euo pipefail

# DD_API_KEY가 없는 환경(로컬 검증 등)에서는 설치를 건너뛴다. 배포를 막지 않기 위함.
DD_API_KEY="$(/opt/elasticbeanstalk/bin/get-config environment -k DD_API_KEY 2>/dev/null || true)"
if [[ -z "${DD_API_KEY}" ]]; then
  echo "DD_API_KEY not set; skipping Datadog agent setup"
  exit 0
fi

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

# EB의 로그 파일을 dd-agent가 읽을 수 있어야 한다 (기본 0644라 보통 문제없지만 명시적으로 보장)
setfacl -m u:dd-agent:rX /var/log 2>/dev/null || true

systemctl enable datadog-agent
systemctl restart datadog-agent
