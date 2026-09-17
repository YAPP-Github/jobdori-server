// promptfoo 실행 결과(JSON)를 축별 점수판으로 펼쳐 CSV나 Google Sheets에 쓴다.
// promptfoo 기본 출력은 케이스당 한 칸이라 어느 축에서 깎였는지 보이지 않는다.

import fs from 'node:fs/promises';
import { ResultFailureReason } from 'promptfoo';
import { collectAxisScores, summarizeCases } from './metrics.js';
import { loadAxes } from './judge.js';

const TIER_LABEL = { gate: '게이트', core: '핵심', diag: '진단' };

function coreAverage(axes, scores) {
  const core = axes.filter((a) => a.tier === 'core' && scores.has(a.name));
  if (!core.length) return '';
  return (core.reduce((sum, a) => sum + scores.get(a.name).score, 0) / core.length).toFixed(2);
}

function parseArgs(argv) {
  const [input, ...rest] = argv;
  const opts = { input, csv: null, sheet: null, summarySheet: null };
  for (let i = 0; i < rest.length; i += 2) {
    if (rest[i] === '--csv') opts.csv = rest[i + 1];
    else if (rest[i] === '--sheet') opts.sheet = rest[i + 1];
    else if (rest[i] === '--summary-sheet') opts.summarySheet = rest[i + 1];
    else throw new Error(`Unknown option: ${rest[i]}`);
  }
  if (!opts.input) throw new Error('사용법: npm run scorecard -- <promptfoo 결과 json> [--csv 경로] [--sheet 시트URL] [--summary-sheet 시트URL]');
  if (!opts.csv && !opts.sheet) opts.csv = opts.input.replace(/\.json$/, '') + '-scorecard.csv';
  return opts;
}

export function buildRows(axes, data) {
  const label = data.results?.prompts?.[0]?.label ?? '';
  const model = /model=(\S+)/.exec(label)?.[1] ?? '';
  const version = /prompt=(\S+)/.exec(label)?.[1] ?? '';

  return data.results.results.map((result) => {
    const scores = new Map(collectAxisScores(result.gradingResult, []).map((s) => [s.axis, s]));
    // 모델 호출이나 채점 자체가 실패한 경우는 프롬프트 품질과 무관하다. 채점 실패와 섞이면 오독한다.
    const errored = result.failureReason === ResultFailureReason.ERROR;
    const row = {
      케이스: result.testCase?.vars?.case_id ?? '',
      '노린 실패 유형': result.testCase?.vars?.note ?? '',
      입력: result.testCase?.vars?.input ?? '',
      판정: errored ? '에러' : result.gradingResult?.pass ? '통과' : '실패',
      // promptfoo의 gradingResult.score는 judge 점수와 __expected 룰 점수를 뭉뚱그린 평균이라 쓸 수 없다.
      // 루브릭의 core 축만 골라 직접 평균한다.
      '핵심 평균': errored ? '' : coreAverage(axes, scores),
    };
    for (const axis of axes) {
      row[`${axis.name} [${TIER_LABEL[axis.tier]}]`] = scores.get(axis.name)?.score ?? '';
    }
    const ruleFailures = (result.gradingResult?.componentResults ?? [])
      .filter((c) => c.assertion?.type && c.pass === false)
      .map((c) => `룰(${c.assertion.type}) 실패: ${c.reason}`);
    row['판정 근거'] = errored
      ? `채점하지 못했다(프롬프트 품질 문제가 아니다): ${String(result.error ?? '').split('\n')[0].slice(0, 300)}`
      : [
        ...axes.map((axis) => (scores.has(axis.name) ? `${axis.name}: ${scores.get(axis.name).reason}` : null)),
        ...ruleFailures,
      ].filter(Boolean).join('\n');
    row.모델 = model;
    row['프롬프트 버전'] = version;
    return row;
  });
}

function toCsv(rows) {
  const headers = Object.keys(rows[0]);
  const cell = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`;
  return [headers.map(cell).join(','), ...rows.map((r) => headers.map((h) => cell(r[h])).join(','))].join('\n');
}

/** promptfoo와 같은 자격증명(GOOGLE_APPLICATION_CREDENTIALS)과 같은 스코프를 쓴다. */
async function writeSheet(rows, url) {
  const { sheets: googleSheets, auth: googleAuth } = await import('@googleapis/sheets');
  const auth = new googleAuth.GoogleAuth({ scopes: ['https://www.googleapis.com/auth/spreadsheets'] });
  const sheets = googleSheets('v4');
  const spreadsheetId = /\/d\/([^/]+)/.exec(url)?.[1];
  if (!spreadsheetId) throw new Error(`Google Sheets URL이 아니다: ${url}`);

  const gid = Number(new URL(url).searchParams.get('gid'));
  const meta = await sheets.spreadsheets.get({ spreadsheetId, auth });
  const target = gid
    ? meta.data.sheets?.find((s) => s.properties?.sheetId === gid)
    : meta.data.sheets?.[0];
  if (!target?.properties?.title) throw new Error(`시트 탭을 찾을 수 없다 (gid=${gid || '첫 번째 탭'})`);

  const headers = Object.keys(rows[0]);
  await sheets.spreadsheets.values.update({
    spreadsheetId,
    range: target.properties.title,
    valueInputOption: 'RAW',
    auth,
    requestBody: { values: [headers, ...rows.map((r) => headers.map((h) => r[h] ?? ''))] },
  });
  return target.properties.title;
}

const opts = parseArgs(process.argv.slice(2));
const raw = await fs.readFile(opts.input, 'utf8').catch(() => null);
if (raw === null) {
  console.error(`결과 파일이 없다: ${opts.input}\n앞의 평가(npm run eval)가 결과를 쓰기 전에 끝났다. 평가 출력의 오류를 먼저 확인한다.`);
  process.exit(1);
}
const data = JSON.parse(raw);
const type = /^(\S+)/.exec(data.config?.description ?? '')?.[1];
if (!type) throw new Error('결과 파일에서 PROMPT_TYPE을 읽을 수 없다. run-eval.sh로 만든 결과인지 확인한다.');
const axes = await loadAxes(type);
const rows = buildRows(axes, data);
if (!rows.length) throw new Error('결과에 케이스가 없다.');
const summary = summarizeCases(axes, data.results.results, {
  errorReason: ResultFailureReason.ERROR,
  axisColumn: (axis) => `${axis.name} [${TIER_LABEL[axis.tier]}]`,
});

console.log('케이스별 요약');
console.table(summary.map((s) => ({ 케이스: s.케이스, 통과율: s.통과율, 에러: s.에러, '게이트 실패': s['게이트 실패'], '룰 실패': s['룰 실패'], '핵심 평균': s['핵심 평균'] })));

if (opts.csv) {
  await fs.writeFile(opts.csv, '﻿' + toCsv(rows), 'utf8');
  const summaryCsv = opts.csv.replace(/(\.csv)?$/, '-summary.csv');
  await fs.writeFile(summaryCsv, '﻿' + toCsv(summary), 'utf8');
  console.log(`${rows.length}건을 ${opts.csv}에, 케이스 ${summary.length}개 요약을 ${summaryCsv}에 썼다.`);
}
if (opts.sheet) {
  const tab = await writeSheet(rows, opts.sheet);
  console.log(`${rows.length}건을 시트 "${tab}"에 썼다.`);
}
if (opts.summarySheet) {
  const tab = await writeSheet(summary, opts.summarySheet);
  console.log(`케이스 ${summary.length}개 요약을 시트 "${tab}"에 썼다.`);
}
