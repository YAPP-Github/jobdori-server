#!/usr/bin/env bash
set -euo pipefail

output="$(dirname "$0")/prompts.json"
tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT

psql --no-psqlrc --set ON_ERROR_STOP=1 --tuples-only --no-align --field-separator='' <<'SQL' > "$tmp"
SELECT COALESCE(json_agg(json_build_object(
  'type', p.type,
  'prompt_id', p.id,
  'prompt_updated_at', p.updated_at,
  'system_prompt', p.content,
  'json_schema', p.json_schema::json,
  'model_name', m.name,
  'parameters', c.parameters
) ORDER BY p.id), '[]'::json)
FROM prompts_v1 p
LEFT JOIN ai_model_configs_v1 c ON c.id = p.ai_model_config_id
LEFT JOIN ai_models_v1 m ON m.id = c.ai_model_id
WHERE p.deleted_at IS NULL
  AND p.type NOT IN ('DOCUMENT_TEXT_EXTRACTION', 'JD_KEY_POINTS');
SQL

mv "$tmp" "$output"
