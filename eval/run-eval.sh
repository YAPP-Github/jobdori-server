#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

: "${PROMPT_TYPE:?PROMPT_TYPE is required (e.g. PROMPT_TYPE=JD_META_EXTRACTION)}"

eval "$(node -e '
const fs = require("node:fs");
const type = process.env.PROMPT_TYPE;
const found = JSON.parse(fs.readFileSync("prompts.json", "utf8")).find((p) => p.type === type);
if (!found) { console.error(`Unknown PROMPT_TYPE: ${type}`); process.exit(1); }
if (!found.model_name) { console.error(`prompts.json has no model_name for ${type}. Re-run npm run dump.`); process.exit(1); }
const q = (v) => `'"'"'${String(v).replace(/'"'"'/g, "")}'"'"'`;
console.log(`export MODEL_NAME=${q(found.model_name)} PROMPT_ID=${q(found.prompt_id)} PROMPT_UPDATED_AT=${q(found.prompt_updated_at)}`);
')"

# 골든셋은 저장소에 두지 않는다. 항상 Google Sheets URL이나 로컬 파일 경로를 GOLDENSET으로 받는다.
if [ -z "${GOLDENSET:-}" ]; then
  echo "GOLDENSET이 필요하다. 골든셋은 저장소에 없고 시트나 파일에서 받는다." >&2
  echo "  GOLDENSET=https://docs.google.com/spreadsheets/d/SHEET_ID/edit PROMPT_TYPE=$PROMPT_TYPE npm run eval" >&2
  echo "  GOLDENSET=~/Downloads/$PROMPT_TYPE.csv PROMPT_TYPE=$PROMPT_TYPE npm run eval" >&2
  exit 1
fi
if [ -f "$GOLDENSET" ]; then
  GOLDENSET="file://$(cd "$(dirname "$GOLDENSET")" && pwd)/$(basename "$GOLDENSET")"
fi
export GOLDENSET

exec npx promptfoo eval -c promptfooconfig.yaml "$@"
