import fs from 'node:fs/promises';

const prompts = JSON.parse(await fs.readFile(new URL('./prompts.json', import.meta.url), 'utf8'));

// promptfoo가 max_tokens 미지정 시 1024를 강제 주입한다. 운영은 max_tokens를 아예 보내지 않으므로
// maxTokens가 없는 타입은 잘리지 않도록 넉넉한 값을 명시한다.
const UNLIMITED_MAX_TOKENS = 16384;

export function findPrompt(type) {
  const prompt = prompts.find((item) => item.type === type);
  if (!prompt) throw new Error(`Unknown PROMPT_TYPE: ${type}`);
  return prompt;
}

export default async function buildPrompt({ vars }) {
  const type = process.env.PROMPT_TYPE;
  const prompt = findPrompt(type);
  const config = {
    max_tokens: prompt.parameters.maxTokens ?? UNLIMITED_MAX_TOKENS,
    // temperature는 passthrough로 보낸다. promptfoo는 falsy 검사로 0을 body에서 떨어뜨린다.
    passthrough: { temperature: prompt.parameters.temperature },
  };
  if (prompt.json_schema) {
    config.response_format = {
      type: 'json_schema',
      json_schema: { name: `${type.toLowerCase()}_result`, strict: true, schema: prompt.json_schema },
    };
  }
  return {
    prompt: [
      { role: 'system', content: prompt.system_prompt },
      { role: 'user', content: String(vars.input ?? '') },
    ],
    config,
  };
}
