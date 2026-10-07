// 골든셋 케이스마다 __expected로 반복해 적지 않도록, 프롬프트가 모든 출력에 요구하는 형식 규칙을 타입별로 둔다.
// 규칙이 없는 타입은 통과시킨다.

// 프롬프트의 writing_rules 5번이 금지하는 범용 동사
const GENERIC_VERBS = ['진행함', '수행함', '실시함', '향상시킴', '기여함'];
// 프롬프트는 "30자 내외"라 30자 초과를 바로 실패로 보지 않는다.
const TITLE_MAX = 35;

const sentences = (text) => text.trim().split(/(?<=[.!?])\s+/).filter(Boolean);

// 개조식 종결(~함, ~됨, ~시킴)은 마지막 음절의 받침이 ㅁ이다.
function endsWithNominal(text) {
  const code = text.trim().at(-1).charCodeAt(0) - 0xac00;
  return code >= 0 && code < 11172 && code % 28 === 16;
}

const RULES = {
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
  const parsed = typeof output === 'string' ? JSON.parse(output) : output;
  const failures = check(parsed, String(context?.vars?.input ?? ''));
  return { pass: failures.length === 0, score: failures.length === 0 ? 1 : 0, reason: failures.join(' / ') || '형식 규칙 통과' };
}
