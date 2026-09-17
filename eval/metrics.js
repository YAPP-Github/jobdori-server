// 설계서가 judge 대신 계산으로 채점하라고 지정한 축들. judge 호출 없이 정답과 출력만으로 점수가 나온다.

function asObject(output) {
  return typeof output === 'string' ? JSON.parse(output) : output;
}

/** 모델 출력의 matchRate 내림차순을 index 순위 배열로 바꾼다. 동점은 그대로 동점으로 둔다. */
export function modelRanking(output) {
  const scores = asObject(output).scores ?? [];
  return scores.map((s) => ({ index: s.index, value: s.matchRate }));
}

/** "3,1,2,5,4" -> 앞이 좋은 순. 동점은 "3=1,2"처럼 등호로 묶는다. */
export function parseExpectedRanking(text) {
  const groups = String(text).split(',').map((part) => part.trim()).filter(Boolean);
  const ranks = new Map();
  groups.forEach((group, position) => {
    for (const raw of group.split('=')) {
      const index = Number.parseInt(raw.trim(), 10);
      if (Number.isNaN(index)) throw new Error(`Invalid expected_ranking entry: ${raw}`);
      ranks.set(index, groups.length - position);
    }
  });
  return ranks;
}

/** Kendall tau-b. 모델이 동점을 많이 내므로 동점 보정이 있는 tau-b를 쓴다. */
export function kendallTauB(pairs) {
  const n = pairs.length;
  if (n < 2) throw new Error('kendall tau needs at least 2 items');
  let concordant = 0;
  let discordant = 0;
  let tiedX = 0;
  let tiedY = 0;
  for (let i = 0; i < n; i += 1) {
    for (let j = i + 1; j < n; j += 1) {
      const dx = Math.sign(pairs[i].x - pairs[j].x);
      const dy = Math.sign(pairs[i].y - pairs[j].y);
      if (dx === 0 && dy === 0) { tiedX += 1; tiedY += 1; continue; }
      if (dx === 0) { tiedX += 1; continue; }
      if (dy === 0) { tiedY += 1; continue; }
      if (dx === dy) concordant += 1; else discordant += 1;
    }
  }
  const n0 = (n * (n - 1)) / 2;
  const denominator = Math.sqrt((n0 - tiedX) * (n0 - tiedY));
  if (denominator === 0) return 0;
  return (concordant - discordant) / denominator;
}

/** 앵커가 5/3/1만 있으므로 4와 2는 그 사이 구간으로 둔다. 임계값은 baseline 보고 조정한다. */
export function tauToScore(tau) {
  if (tau >= 0.8) return 5;
  if (tau >= 0.6) return 4;
  if (tau >= 0.4) return 3;
  if (tau >= 0.2) return 2;
  return 1;
}

export function rankingScore(output, expectedText) {
  const ranks = parseExpectedRanking(expectedText);
  const model = modelRanking(output).filter((item) => ranks.has(item.index));
  if (model.length < 2) throw new Error('expected_ranking does not overlap the model output indexes');
  const tau = kendallTauB(model.map((item) => ({ x: item.value, y: ranks.get(item.index) })));
  return { score: tauToScore(tau), detail: `kendall tau-b ${tau.toFixed(3)} (n=${model.length})` };
}

/** "1:12,2:null,3:7" -> index별 정답 매칭 대상. null은 신규(중복 아님). */
export function parseExpectedMatches(text) {
  const map = new Map();
  for (const part of String(text).split(',')) {
    const [rawIndex, rawId] = part.split(':').map((v) => v.trim());
    const index = Number.parseInt(rawIndex, 10);
    if (Number.isNaN(index)) throw new Error(`Invalid expected_matches entry: ${part}`);
    map.set(index, rawId === 'null' || rawId === '' ? null : Number.parseInt(rawId, 10));
  }
  return map;
}

/**
 * 오병합 precision. 모델이 중복이라고 붙인 건 중 정답과 다른 대상에 붙인 게 하나라도 있으면 1점이다.
 * 설계서 03장: 오병합 1건도 되돌릴 수 없는 유저 데이터 손실이라 게이트로 둔다.
 */
export function matchPrecisionScore(output, expectedText, idField) {
  const expected = parseExpectedMatches(expectedText);
  const items = asObject(output).items ?? [];
  const claimed = items.filter((item) => item[idField] !== null && item[idField] !== undefined);
  const wrong = claimed.filter((item) => expected.get(item.index) !== item[idField]);
  const precision = claimed.length === 0 ? 1 : (claimed.length - wrong.length) / claimed.length;
  const detail = `precision ${precision.toFixed(3)} (중복 판정 ${claimed.length}건 중 오병합 ${wrong.length}건`
    + `${wrong.length ? `: index ${wrong.map((w) => w.index).join(', ')}` : ''})`;
  return { score: wrong.length === 0 ? 5 : 1, detail };
}

/** 설계서 04장: 순서를 뒤집어 두 번 돌리고 일치할 때만 채택한다. */
export function pairwiseScore(firstWinner, secondWinner) {
  if (firstWinner !== secondWinner) {
    return { score: 3, detail: `위치 편향 감지: 순서를 바꾸자 판정이 ${firstWinner} -> ${secondWinner}로 뒤집힘` };
  }
  return firstWinner === 'polished'
    ? { score: 5, detail: '순서를 바꿔도 첨삭본이 낫다고 일관되게 판정' }
    : { score: 1, detail: '순서를 바꿔도 원문이 낫다고 일관되게 판정' };
}

/** promptfoo 결과에서 축 점수만 뽑는다. componentResults는 assert 배열 때문에 한 겹 더 중첩된다. */
export function collectAxisScores(gradingResult, into) {
  for (const component of gradingResult?.componentResults ?? []) {
    const matched = /^(.*) \[(gate|core|diag)\]$/.exec(component.component ?? '');
    if (matched) into.push({ axis: matched[1], tier: matched[2], score: component.score, reason: component.reason });
    else collectAxisScores(component, into);
  }
  return into;
}

/**
 * 같은 케이스를 여러 번 채점한 결과에서 축별 점수 흔들림을 집계한다.
 * 설계서 04장: 점수가 1점 넘게 벌어지는 축은 앵커가 모호한 것이므로 결과를 쓰지 말고 앵커부터 고친다.
 */
export function agreementRows(results) {
  const byKey = new Map();
  for (const result of results) {
    const caseId = result.testCase?.vars?.case_id ?? result.testCase?.description ?? 'unknown';
    for (const { axis, tier, score } of collectAxisScores(result.gradingResult, [])) {
      const key = `${axis}\u0000${caseId}`;
      if (!byKey.has(key)) byKey.set(key, { axis, tier, caseId, scores: [] });
      byKey.get(key).scores.push(score);
    }
  }

  const byAxis = new Map();
  for (const entry of byKey.values()) {
    if (entry.scores.length < 2) continue;
    const spread = Math.max(...entry.scores) - Math.min(...entry.scores);
    if (!byAxis.has(entry.axis)) byAxis.set(entry.axis, { axis: entry.axis, tier: entry.tier, cases: 0, unstable: 0, maxSpread: 0, worstCase: null });
    const row = byAxis.get(entry.axis);
    row.cases += 1;
    if (spread >= 2) row.unstable += 1;
    if (spread > row.maxSpread) { row.maxSpread = spread; row.worstCase = entry.caseId; }
  }
  return [...byAxis.values()].sort((a, b) => b.unstable - a.unstable || b.maxSpread - a.maxSpread);
}

/**
 * --repeat로 같은 케이스를 여러 번 돌린 결과를 케이스별로 묶는다.
 * temperature가 0보다 큰 프롬프트는 같은 입력에서도 판정이 뒤집히므로 1회 판정이 아니라 통과율로 봐야 한다.
 * 에러(모델 호출/채점 실패)는 프롬프트 품질과 무관하므로 통과율 분모에서 뺀다.
 */
export function summarizeCases(axes, results, { errorReason, axisColumn = (axis) => axis.name } = {}) {
  const mean = (values) => (values.length ? (values.reduce((a, b) => a + b, 0) / values.length).toFixed(2) : '');
  const groups = new Map();
  for (const result of results) {
    const id = result.testCase?.vars?.case_id ?? 'unknown';
    if (!groups.has(id)) groups.set(id, { note: result.testCase?.vars?.note ?? '', runs: [] });
    groups.get(id).runs.push(result);
  }

  return [...groups].map(([id, { note, runs }]) => {
    const graded = runs.filter((r) => r.failureReason !== errorReason);
    const passed = graded.filter((r) => r.gradingResult?.pass).length;
    const scores = graded.map((r) => new Map(collectAxisScores(r.gradingResult, []).map((s) => [s.axis, s.score])));
    const core = axes.filter((a) => a.tier === 'core');
    const coreAverages = scores
      .map((m) => core.map((a) => m.get(a.name)).filter((v) => v !== undefined))
      .filter((values) => values.length)
      .map((values) => values.reduce((a, b) => a + b, 0) / values.length);

    const row = {
      케이스: id,
      '노린 실패 유형': note,
      '실행 수': runs.length,
      통과율: graded.length ? `${passed}/${graded.length} (${Math.round((passed / graded.length) * 100)}%)` : '',
      에러: runs.length - graded.length,
      '게이트 실패': scores.filter((m) => axes.some((a) => a.tier === 'gate' && m.get(a.name) === 1)).length,
      '룰 실패': graded.filter((r) =>
        (r.gradingResult?.componentResults ?? []).some((c) => c.assertion?.type && c.pass === false)).length,
      '핵심 평균': mean(coreAverages),
    };
    for (const axis of axes) {
      row[axisColumn(axis)] = mean(scores.map((m) => m.get(axis.name)).filter((v) => v !== undefined));
    }
    return row;
  });
}
