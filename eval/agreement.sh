#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

: "${PROMPT_TYPE:?PROMPT_TYPE is required}"
runs="${AGREEMENT_RUNS:-2}"
out="$(mktemp -t agreement).json"
trap 'rm -f "$out"' EXIT

# 캐시가 켜져 있으면 두 번째 채점이 첫 번째 judge 응답을 그대로 받아 흔들림이 0으로 보인다.
# --no-cache는 대상 모델 호출만 막고 judge 호출은 계속 캐시되므로 환경변수로 전체를 끈다.
export PROMPTFOO_CACHE_ENABLED=false
# 케이스가 fail이면 promptfoo가 0이 아닌 코드로 끝나는데, 합의도 측정에서는 fail도 정상 데이터다.
./run-eval.sh --repeat "$runs" --no-cache --output "$out" "$@" >/dev/null || true
[ -s "$out" ] || { echo "평가 결과가 없다. 먼저 npm run eval로 실행이 되는지 확인한다." >&2; exit 1; }

node -e '
const fs = require("node:fs");
import("./metrics.js").then(({ agreementRows }) => {
  const data = JSON.parse(fs.readFileSync(process.argv[1], "utf8"));
  const rows = agreementRows(data.results.results);
  if (!rows.length) { console.log("집계할 축이 없다. 골든셋이 비어 있는지 확인한다."); return; }
  console.log(`${process.env.PROMPT_TYPE} 합의도 (${process.env.AGREEMENT_RUNS || 2}회 채점, judge temperature ${process.env.JUDGE_TEMPERATURE || 0})`);
  console.log("축\ttier\t케이스\t2점이상흔들림\t최대폭\t최악케이스");
  for (const r of rows) {
    console.log(`${r.axis}\t${r.tier}\t${r.cases}\t${r.unstable}\t${r.maxSpread}\t${r.worstCase ?? "-"}`);
  }
  const bad = rows.filter((r) => r.unstable > 0);
  console.log(bad.length
    ? `\n앵커를 고쳐야 할 축: ${bad.map((r) => r.axis).join(", ")}. 이 축들의 점수는 아직 쓰지 않는다.`
    : "\n2점 이상 벌어진 축 없음.");
});
' "$out"
