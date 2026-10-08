// 골든셋 케이스마다 __expected로 반복해 적지 않도록, 프롬프트가 모든 출력에 요구하는 형식 규칙을 타입별로 둔다.
// 규칙이 없는 타입은 통과시킨다.

// 프롬프트의 writing_rules 5번이 금지하는 범용 동사
const GENERIC_VERBS = ['진행함', '수행함', '실시함', '향상시킴', '기여함'];
// 프롬프트는 "30자 내외"라 30자 초과를 바로 실패로 보지 않는다.
const TITLE_MAX = 35;

// PROFILE_CORE_COMPETENCY_JD_TEMPLATE 프롬프트의 [최종 검증] 목록
const BANNED_CONNECTIVES = ['특히', '또한', '통해', '이러한', '바탕으로', '나아가', '결과적으로', '그리고', '하지만', '따라서'];
const BANNED_CLAIMS = ['해왔습니다', '경험이 있습니다', '강점이 있습니다', '기여', '보탬', '만들어가겠습니다', '귀사', '함께 성장'];
const PLACEHOLDER = /\[\[([^\]]*)\]\]/g;
const SKELETON_MAX = 400;

const sentences = (text) => text.trim().split(/(?<=[.!?])\s+/).filter(Boolean);

// 개조식 종결(~함, ~됨, ~시킴)은 마지막 음절의 받침이 ㅁ이다.
function endsWithNominal(text) {
  const code = text.trim().at(-1).charCodeAt(0) - 0xac00;
  return code >= 0 && code < 11172 && code % 28 === 16;
}

const RULES = {
  PROFILE_CORE_COMPETENCY_JD_TEMPLATE: (output, input) => {
    const failures = [];
    const text = String(output).trim();
    if (text.includes('\n')) failures.push('문단 하나가 아님(줄바꿈 포함)');
    const all = sentences(text);
    if (all.length < 4 || all.length > 5) failures.push(`문장 ${all.length}개(4~5개)`);
    if (!/\[\[이름\]\]입니다[.]?$/.test(all[0]?.trim() ?? '')) failures.push(`헤드라인이 "[[이름]]입니다"로 끝나지 않음: ${all[0]}`);
    all.forEach((sentence, i) => {
      const slots = [...sentence.matchAll(PLACEHOLDER)].map((m) => m[1].trim()).filter((s) => s !== '이름');
      if (slots.length !== 1) failures.push(`문장 ${i + 1} 자리표시자 ${slots.length}개(1개): ${sentence}`);
      for (const slot of slots) {
        const m = slot.match(/^(.+?)\s*\(예:\s*(.+)\)$/);
        if (!m) { failures.push(`예시 없는 자리표시자: [[${slot}]]`); continue; }
        // 프롬프트 자체 예시("문제를 찾을 때 가장 먼저 보는 것")가 공백 포함 19자라 15자는 공백 제외로 센다.
        const label = m[1].replace(/\s/g, '');
        if (label.length > 15) failures.push(`자리표시자 내용 공백 제외 ${label.length}자(15자 이내): ${m[1]}`);
        if (m[2].split(',').map((s) => s.trim()).filter(Boolean).length !== 2) failures.push(`예시가 2개가 아님: ${m[2]}`);
      }
    });
    const skeleton = text.replace(PLACEHOLDER, (_, slot) => `[[${slot.replace(/\s*\(예:.*\)$/, '')}]]`);
    if (skeleton.length > SKELETON_MAX) failures.push(`예시 제외 ${skeleton.length}자(${SKELETON_MAX}자 이내)`);
    const prose = text.replace(PLACEHOLDER, '');
    BANNED_CONNECTIVES.filter((w) => prose.includes(w)).forEach((w) => failures.push(`접속 표현 "${w}"`));
    BANNED_CLAIMS.filter((w) => prose.includes(w)).forEach((w) => failures.push(`경험 단정/포부 표현 "${w}"`));
    if (/\d/.test(prose)) failures.push(`수치 포함: ${prose.match(/[^ ]*\d[^ ]*/)[0]}`);
    const company = input.match(/^\[기업명\]\s*(.+)$/m)?.[1]?.trim();
    if (company && text.includes(company)) failures.push(`기업명 "${company}" 노출`);
    return failures;
  },
  EXPERIENCE_QUESTION_ROOM_CHAT: ({ block, feedback, question, status }, input) => {
    const failures = [];
    if (block.title.length > TITLE_MAX) failures.push(`제목 ${block.title.length}자(${TITLE_MAX}자 이하)`);
    if (/경험$/.test(block.title.trim())) failures.push(`"~경험" 제목: ${block.title}`);
    block.bullets.forEach((bullet, i) => {
      if (/[.。]$/.test(bullet.trim())) failures.push(`본문 ${i + 1} 마침표로 끝남: ${bullet}`);
      else if (!endsWithNominal(bullet)) failures.push(`본문 ${i + 1} 개조식 종결 아님: ${bullet}`);
      if (sentences(bullet).length > 1) failures.push(`본문 ${i + 1} 여러 문장: ${bullet}`);
    });
    GENERIC_VERBS.filter((verb) => block.bullets.some((bullet) => bullet.includes(verb)))
      .forEach((verb) => failures.push(`범용 동사 "${verb}"`));
    if (sentences(feedback.fit).length !== 1) failures.push(`fit 한 문장 아님: ${feedback.fit}`);
    const firstTurn = !/^## 현재 블록/m.test(input);
    if (firstTurn && feedback.changed !== '') failures.push(`첫 생성인데 changed가 비어 있지 않음: ${feedback.changed}`);
    if (!firstTurn && sentences(feedback.changed).length !== 1) failures.push(`changed 한 문장 아님: ${feedback.changed}`);
    if (status === 'complete' && question !== '') failures.push(`complete인데 question이 있음: ${question}`);
    if (status === 'in_progress' && !question.trim()) failures.push('in_progress인데 question이 비어 있음');
    return failures;
  },
};

export default function rules(output, context) {
  const check = RULES[process.env.PROMPT_TYPE];
  if (!check) return true;
  const parsed = typeof output === 'string' && output.trim().startsWith('{') ? JSON.parse(output) : output;
  const failures = check(parsed, String(context?.vars?.input ?? ''));
  return { pass: failures.length === 0, score: failures.length === 0 ? 1 : 0, reason: failures.join(' / ') || '형식 규칙 통과' };
}
