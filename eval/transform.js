const SECTIONS = { 담당업무: 'RESPONSIBILITY', 우대사항: 'PREFERRED_EXPERIENCE' };

// 서버(GetExperienceQuestionRoomsService)가 index로 JD 원문을 찾아 sourceText를 채우는 것을 재현한다.
// 없는 번호는 서버처럼 카드를 버리지 않고 sourceText를 null로 남겨 출처정확성 게이트가 잡게 한다.
function withSourceText(output, input) {
  const items = { RESPONSIBILITY: [], PREFERRED_EXPERIENCE: [] };
  let current = null;
  for (const line of input.split('\n')) {
    const header = line.match(/^##\s*(.+?)\s*$/);
    if (header) {
      current = SECTIONS[header[1]] ?? null;
      continue;
    }
    const item = line.match(/^\[(\d+)\]\s*(.*)$/);
    if (current && item) items[current][Number(item[1]) - 1] = item[2].trim();
  }
  const parsed = typeof output === 'string' ? JSON.parse(output) : output;
  return {
    ...parsed,
    cards: (parsed.cards ?? []).map((card) => ({
      ...card,
      sourceText: items[card.sourceType]?.[card.index - 1] ?? null,
    })),
  };
}

export default function transform(output, context) {
  if (process.env.PROMPT_TYPE !== 'EXPERIENCE_QUESTION_ROOM_GENERATION') return output;
  return withSourceText(output, String(context.vars?.input ?? ''));
}
