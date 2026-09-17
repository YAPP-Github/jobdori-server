import assert from 'node:assert/strict';
import test from 'node:test';
import { loadAxes, toGradingResult } from './judge.js';

const axes = [
  { name: '환각없음', tier: 'gate' },
  { name: '필드귀속정확성', tier: 'core' },
  { name: '커버리지', tier: 'diag' },
];

test('gate 1점이면 fail이고 core 평균은 gate/diag를 빼고 계산한다', () => {
  const result = toGradingResult(axes, { verdict: 'fail', scores: [
    { axis: '환각없음', rationale: '"없는 회사명"', score: 1 },
    { axis: '필드귀속정확성', rationale: '"정확"', score: 5 },
    { axis: '커버리지', rationale: '"누락"', score: 1 },
  ] });
  assert.equal(result.pass, false);
  assert.equal(result.score, 5);
  assert.equal(result.componentResults.length, 3);
});

test('축이 빠지면 조용히 통과시키지 않고 실패한다', () => {
  assert.throws(
    () => toGradingResult(axes, { verdict: 'pass', scores: [{ axis: '환각없음', rationale: 'r', score: 5 }] }),
    /필드귀속정확성/,
  );
});

test('루브릭 축은 공통 게이트와 타입 고유 축이 합쳐진다', async () => {
  const loaded = await loadAxes('JD_META_EXTRACTION');
  assert.deepEqual(loaded.map((a) => `${a.name}:${a.tier}`), [
    '환각없음:gate', '필드귀속정확성:core', '원문충실성:core', '커버리지:diag', '항목분해품질:diag',
  ]);
});
