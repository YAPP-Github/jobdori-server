import assert from 'node:assert/strict';
import test from 'node:test';
import { agreementRows, matchPrecisionScore, pairwiseScore, rankingScore, summarizeCases } from './metrics.js';

const recommendation = (rates) => ({ scores: rates.map((matchRate, i) => ({ index: i + 1, matchRate })), reasons: [] });

test('정답 순위와 같은 순서면 tau 1로 5점', () => {
  const { score, detail } = rankingScore(recommendation([90, 70, 50]), '1,2,3');
  assert.equal(score, 5);
  assert.match(detail, /kendall tau-b 1\.000/);
});

test('정답 순위와 반대면 1점', () => {
  assert.equal(rankingScore(recommendation([90, 70, 50]), '3,2,1').score, 1);
});

test('모델이 전부 동점이면 tau 0으로 1점 - 실사용에서 관측된 고정 점수 반복을 잡는다', () => {
  const { score, detail } = rankingScore(recommendation([68, 68, 68]), '1,2,3');
  assert.equal(score, 1);
  assert.match(detail, /kendall tau-b 0\.000/);
});

test('오병합이 하나라도 있으면 게이트 1점', () => {
  const output = { items: [
    { index: 1, matchedProjectId: 12 },
    { index: 2, matchedProjectId: 99 },
    { index: 3, matchedProjectId: null },
  ] };
  const { score, detail } = matchPrecisionScore(output, '1:12,2:34,3:null', 'matchedProjectId');
  assert.equal(score, 1);
  assert.match(detail, /오병합 1건: index 2/);
});

test('중복 판정이 전부 정답과 맞으면 5점', () => {
  const output = { items: [{ index: 1, matchedProjectId: 12 }, { index: 2, matchedProjectId: null }] };
  assert.equal(matchPrecisionScore(output, '1:12,2:null', 'matchedProjectId').score, 5);
});

test('순서를 뒤집었을 때 판정이 갈리면 채택하지 않고 3점', () => {
  assert.equal(pairwiseScore('polished', 'original').score, 3);
  assert.equal(pairwiseScore('polished', 'polished').score, 5);
  assert.equal(pairwiseScore('original', 'original').score, 1);
});

const repeated = (scores) => scores.map((score) => ({
  testCase: { vars: { case_id: 'c1' } },
  gradingResult: { componentResults: [{ componentResults: [
    { component: '원문충실성 [core]', score },
    { component: '환각없음 [gate]', score: 5 },
  ] }] },
}));

test('두 번 채점해 2점 넘게 벌어진 축을 앞으로 올린다', () => {
  const rows = agreementRows(repeated([5, 2]));
  assert.equal(rows[0].axis, '원문충실성');
  assert.equal(rows[0].unstable, 1);
  assert.equal(rows[0].maxSpread, 3);
  assert.equal(rows[1].unstable, 0);
});

test('한 번만 채점한 결과는 흔들림을 잴 수 없으므로 집계에서 뺀다', () => {
  assert.deepEqual(agreementRows(repeated([4])), []);
});

test('반복 실행을 케이스별로 묶고, 에러는 통과율 분모에서 뺀다', () => {
  const axes = [{ name: '환각없음', tier: 'gate' }, { name: '변별력', tier: 'core' }];
  const run = (pass, gate, core, extra = {}) => ({
    testCase: { vars: { case_id: 'c1', note: 'n' } },
    gradingResult: { pass, componentResults: [
      { componentResults: [{ component: '환각없음 [gate]', score: gate }, { component: '변별력 [core]', score: core }] },
      ...(extra.ruleFail ? [{ assertion: { type: 'javascript' }, pass: false }] : []),
    ] },
    failureReason: extra.error ? 2 : pass ? 0 : 1,
  });
  const [row] = summarizeCases(axes, [
    run(true, 5, 4),
    run(false, 1, 2),
    run(false, 5, 3, { ruleFail: true }),
    { testCase: { vars: { case_id: 'c1', note: 'n' } }, failureReason: 2 },
  ], { errorReason: 2 });
  assert.equal(row['실행 수'], 4);
  assert.equal(row.통과율, '1/3 (33%)');
  assert.equal(row.에러, 1);
  assert.equal(row['게이트 실패'], 1);
  assert.equal(row['룰 실패'], 1);
  assert.equal(row['핵심 평균'], '3.00');
});
