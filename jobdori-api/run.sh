#!/bin/bash
# LLM Observability agentless 모드는 앱 프로세스에 DD_API_KEY가 있어야 한다.
# EB 환경 속성에 평문으로 두지 않기 위해 기동 시점에 Secrets Manager에서 읽는다.
# 키 조회에 실패해도 애플리케이션은 기동해야 하므로 set -e를 쓰지 않는다.
set -uo pipefail

if [[ -z "${DD_API_KEY:-}" && -n "${DD_API_KEY_SECRET_ID:-}" ]]; then
  secret_string="$(aws secretsmanager get-secret-value \
    --secret-id "${DD_API_KEY_SECRET_ID}" \
    --region "${AWS_REGION:-ap-northeast-2}" \
    --query SecretString --output text 2>/dev/null || true)"

  # 평문 시크릿이면 그대로, JSON이면 DD_API_KEY 필드만 쓴다
  DD_API_KEY="$(printf '%s' "${secret_string}" | python3 -c '
import json, sys
raw = sys.stdin.read().strip()
try:
    parsed = json.loads(raw)
except Exception:
    print(raw)
else:
    print(parsed.get("DD_API_KEY", "") if isinstance(parsed, dict) else raw)
' 2>/dev/null || true)"
  export DD_API_KEY

  if [[ -z "${DD_API_KEY}" ]]; then
    echo "WARNING: DD_API_KEY not resolved; LLM Observability will not report" >&2
  fi
fi

exec java --enable-native-access=ALL-UNNAMED -javaagent:dd-java-agent.jar -jar application.jar
