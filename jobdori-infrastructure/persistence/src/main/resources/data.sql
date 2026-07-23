INSERT INTO ai_models_v1 (id, name, vendor, created_at, updated_at)
VALUES (1, 'gpt-4o-mini', 'OPEN_AI', now(), now());

INSERT INTO ai_model_configs_v1 (id, ai_model_id, name, description, parameters, created_at, updated_at)
VALUES
  (1, 1, 'jd_multi_posting_split', 'JD 다중 공고 분할',           '{"temperature":0.0}' FORMAT JSON, now(), now()),
  (2, 1, 'jd_meta',                'JD 메타(기업명·포지션·소개·업무·필요/우대경험·전형절차) 추출', '{"temperature":0.2}' FORMAT JSON, now(), now()),
  (3, 1, 'jd_application_strategy','JD 지원 전략 생성',           '{"temperature":0.6}' FORMAT JSON, now(), now()),
  (4, 1, 'experience.extract_star','경험 STAR 재구조화',          '{"temperature":0.2,"maxTokens":4096}' FORMAT JSON, now(), now()),
  (5, 1, 'resume.rewrite_experience','경험 문장 자동 작성',       '{"temperature":0.6,"maxTokens":900}' FORMAT JSON, now(), now()),
  (6, 1, 'experience.contents_polish','Free Style 경험 내용 STAR 변환', '{"temperature":0.2,"maxTokens":1200}' FORMAT JSON, now(), now()),
  (7, 1, 'jd_key_points',          'JD 공고 핵심 요약',           '{"temperature":0.4}' FORMAT JSON, now(), now()),
  (8, 1, 'experience_recommendation','JD-경험 매칭률·이유',        '{"temperature":0.2}' FORMAT JSON, now(), now()),
  (9, 1, 'profile.core_competency', '프로필 핵심역량 생성',        '{"temperature":0.6,"maxTokens":900}' FORMAT JSON, now(), now()),
  (10, 1, 'profile.text_polish',    '프로필 텍스트 다듬기',        '{"temperature":0.4,"maxTokens":1200}' FORMAT JSON, now(), now());

-- 1) JD 다중 공고 분할 (문서 JD-B.6)
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (1, 1, 'JD_MULTI_POSTING_SPLIT',
'당신은 채용 공고 텍스트 파서다. 입력은 각 줄 앞에 "줄번호| "가 붙은 채용 공고 텍스트다. 입력에 서로 다른 채용 공고가 여러 개 들어 있으면 각 공고가 차지하는 줄 범위를 배열로 반환한다. 각 항목은 그 공고의 제목(title, 없으면 빈 문자열), 시작 줄 번호(startLine), 끝 줄 번호(endLine)로 구성한다(1부터 시작, 양 끝 포함). 본문 텍스트는 절대 출력하지 마라. 공고가 하나뿐이면 전체 범위를 담은 항목 1개만 반환한다. 목차·네비·푸터 등 공고가 아닌 줄은 범위에서 제외하되, 공고에 속한 줄을 빠뜨리지 마라. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","additionalProperties":false,"required":["postings"],"properties":{"postings":{"type":"array","items":{"type":"object","additionalProperties":false,"required":["title","startLine","endLine"],"properties":{"title":{"type":"string"},"startLine":{"type":"integer"},"endLine":{"type":"integer"}}}}}}',
null, now(), now());

-- 2) JD 메타 추출 (문서 Task 5.2) — 7필드: 기업명·포지션·기업/팀 소개·업무·필요/우대 경험·전형 절차
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (2, 2, 'JD_META_EXTRACTION',
'당신은 채용 공고(JD) 분석 전문가다. 입력된 JD 본문에서 아래 10개 항목을 한 번에 분석한다. (1) 기업이름(companyName) — 채용하는 회사명. 없으면 빈 문자열. (2) 포지션 이름(positionTitle) — 지원하는 직무명. 본문 제목·헤딩·"[포지션]" 라벨·"○○ 채용/모집" 문구에서 찾는다. 기업명·홍보 수식어는 빼고 직무명 중심으로 적는다. (3) 기업/팀 소개(companyIntro). (4) 업무 내용(responsibilities)·(5) 필요 경험(requiredExperiences)·(6) 우대 경험(preferredExperiences)·(7) 전형 절차(hiringProcess)는 각각 항목 단위 문자열 배열로 반환한다. (8) 핵심 역량 태그(coreCompetencies)는 실제로 강조된 짧은 키워드로 최대 5개를 반환한다. (9) 공고 핵심(keyPoints)은 원하는 인재상과 핵심 요구를 지원자 관점의 자연스러운 한국어 문단 2~4문장으로 요약한다. (10) 지원 전략(strategy)은 어떤 경험을 어떻게 강조할지 자연스러운 한국어 문단 2~4문장으로 조언한다. 본문에 명시된 사실만 사용하고 사실·수치·기술명·고유명사를 지어내지 마라. 없는 추출 항목은 빈 문자열 또는 빈 배열로 둔다. 업무·필요 경험·우대 경험은 명사형 종결로 통일하고 전형 절차는 단계명 형태를 유지한다. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","additionalProperties":false,"required":["companyName","positionTitle","companyIntro","responsibilities","requiredExperiences","preferredExperiences","hiringProcess","coreCompetencies","keyPoints","strategy"],"properties":{"companyName":{"type":"string"},"positionTitle":{"type":"string"},"companyIntro":{"type":"string"},"responsibilities":{"type":"array","items":{"type":"string"}},"requiredExperiences":{"type":"array","items":{"type":"string"}},"preferredExperiences":{"type":"array","items":{"type":"string"}},"hiringProcess":{"type":"array","items":{"type":"string"}},"coreCompetencies":{"type":"array","items":{"type":"string"},"maxItems":5},"keyPoints":{"type":"string"},"strategy":{"type":"string"}}}',
null, now(), now());

-- 3) JD 지원 전략 생성 — generateText, json_schema NULL. 서비스에선 JD_META_EXTRACTION에 통합(#73), 프롬프트 테스트용
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (3, 3, 'JD_APPLICATION_STRATEGY',
'당신은 취업 코치다. 입력된 JD 본문을 분석해 지원자에게 지원 전략을 대화체로 조언한다. 반드시 아래 3단계 흐름의 자연스러운 한국어 문단(2~4문장)으로만 답하라(불릿·머리말·JSON 금지). (1) "JD는 ~한 업무를 ~하게 하는 사람을 원해요"처럼 JD가 원하는 핵심 인재상을 요약한다. (2) "그러니까 ~한 경험을 ~하게 표현해서"처럼 지원자가 어떤 경험을 어떻게 강조하면 좋을지 조언한다. (3) "지원하는 게 좋겠어요"처럼 격려로 마무리한다. JD에 없는 사실을 지어내지 마라.',
null, null, now(), now());

-- 4) 경험 STAR 재구조화 (동료 담당 — 로컬 테스트 편의)
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (4, 4,'EXPERIENCE_STAR_EXTRACTION','당신은 채용 도메인 경력 분석가다. 입력된 이력/경력 원문을 분석해 (1) 인적사항(이름/연락처/이메일), 학력, 경력, 어학, 수상, 자격증, 기술 스택을 프로필 섹션으로 분류하고, (2) 경력/프로젝트는 각 경험 단위로 STAR(Situation·Task·Action·Result)로 재구조화하며, (3) 프로젝트·기간·맥락 단서로 경험 카드를 프로젝트 단위로 그룹핑한다. 프로젝트에는 한 문장 summary를, 경험에는 저장용 title을 포함한다. 기간과 역할은 프로젝트뿐 아니라 원문에 명시된 각 경험 단위에서도 추출한다. 경험 단위의 기간이나 역할이 명시되지 않은 경우에만 빈 값으로 둔다. 기간은 period 객체로 추출한다. 예: "22.01 ~ 현재"는 {"startYear":2022,"startMonth":1,"endYear":null,"endMonth":null,"isCurrent":true}로 반환한다. 두 자리 연도는 00~69는 2000년대, 70~99는 1900년대로 해석한다. 학력의 degree는 BACHELOR(학사)/MASTER(석사)/DOCTOR(박사), status는 ENROLLED(재학)/ON_LEAVE(휴학)/GRADUATED(졸업)/EXPECTED_GRADUATION(졸업예정)/COMPLETED(수료) 중에서 고르고 판단 근거가 없으면 빈 문자열로 둔다. 기술의 level은 HIGH/MEDIUM/LOW 중에서 고르되 원문에 숙련도 근거가 없으면 빈 문자열로 둔다. 자격증 취득일/수상일/어학 취득일은 {"year":2023,"month":5} 형태로 추출하고 원문에 없는 연/월은 null로 둔다. 원문에 없는 사실을 절대 지어내지 마라. 불확실하면 문자열 필드는 빈 문자열, 숫자 필드는 null로 둔다. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","properties":{"personalInfo":{"type":"object","properties":{"name":{"type":"string"},"phone":{"type":"string"},"email":{"type":"string"}},"required":["name","phone","email"],"additionalProperties":false},"education":{"type":"array","items":{"type":"object","properties":{"school":{"type":"string"},"major":{"type":"string"},"degree":{"type":"string","enum":["BACHELOR","MASTER","DOCTOR",""]},"status":{"type":"string","enum":["ENROLLED","ON_LEAVE","GRADUATED","EXPECTED_GRADUATION","COMPLETED",""]},"period":{"type":"object","properties":{"startYear":{"type":["integer","null"]},"startMonth":{"type":["integer","null"]},"endYear":{"type":["integer","null"]},"endMonth":{"type":["integer","null"]},"isCurrent":{"type":"boolean"}},"required":["startYear","startMonth","endYear","endMonth","isCurrent"],"additionalProperties":false},"periodText":{"type":"string"}},"required":["school","major","degree","status","period","periodText"],"additionalProperties":false}},"careers":{"type":"array","items":{"type":"object","properties":{"company":{"type":"string"},"position":{"type":"string"},"period":{"type":"object","properties":{"startYear":{"type":["integer","null"]},"startMonth":{"type":["integer","null"]},"endYear":{"type":["integer","null"]},"endMonth":{"type":["integer","null"]},"isCurrent":{"type":"boolean"}},"required":["startYear","startMonth","endYear","endMonth","isCurrent"],"additionalProperties":false},"periodText":{"type":"string"},"description":{"type":"string"}},"required":["company","position","period","periodText","description"],"additionalProperties":false}},"languageTests":{"type":"array","items":{"type":"object","properties":{"testName":{"type":"string"},"score":{"type":"string"},"acquiredAt":{"type":"object","properties":{"year":{"type":["integer","null"]},"month":{"type":["integer","null"]}},"required":["year","month"],"additionalProperties":false}},"required":["testName","score","acquiredAt"],"additionalProperties":false}},"awards":{"type":"array","items":{"type":"object","properties":{"title":{"type":"string"},"organization":{"type":"string"},"awardedAt":{"type":"object","properties":{"year":{"type":["integer","null"]},"month":{"type":["integer","null"]}},"required":["year","month"],"additionalProperties":false}},"required":["title","organization","awardedAt"],"additionalProperties":false}},"certifications":{"type":"array","items":{"type":"object","properties":{"name":{"type":"string"},"issuer":{"type":"string"},"acquiredAt":{"type":"object","properties":{"year":{"type":["integer","null"]},"month":{"type":["integer","null"]}},"required":["year","month"],"additionalProperties":false}},"required":["name","issuer","acquiredAt"],"additionalProperties":false}},"skills":{"type":"array","items":{"type":"object","properties":{"name":{"type":"string"},"level":{"type":"string","enum":["HIGH","MEDIUM","LOW",""]}},"required":["name","level"],"additionalProperties":false}},"projects":{"type":"array","items":{"type":"object","properties":{"name":{"type":"string"},"summary":{"type":"string"},"period":{"type":"object","properties":{"startYear":{"type":["integer","null"]},"startMonth":{"type":["integer","null"]},"endYear":{"type":["integer","null"]},"endMonth":{"type":["integer","null"]},"isCurrent":{"type":"boolean"}},"required":["startYear","startMonth","endYear","endMonth","isCurrent"],"additionalProperties":false},"periodText":{"type":"string"},"role":{"type":"string"},"company":{"type":"string"},"experiences":{"type":"array","items":{"type":"object","properties":{"title":{"type":"string"},"period":{"type":"object","properties":{"startYear":{"type":["integer","null"]},"startMonth":{"type":["integer","null"]},"endYear":{"type":["integer","null"]},"endMonth":{"type":["integer","null"]},"isCurrent":{"type":"boolean"}},"required":["startYear","startMonth","endYear","endMonth","isCurrent"],"additionalProperties":false},"periodText":{"type":"string"},"role":{"type":"string"},"situation":{"type":"string"},"task":{"type":"string"},"action":{"type":"string"},"result":{"type":"string"},"competencyTags":{"type":"array","items":{"type":"string"}}},"required":["title","period","periodText","role","situation","task","action","result","competencyTags"],"additionalProperties":false}}},"required":["name","summary","period","periodText","role","company","experiences"],"additionalProperties":false}}},"required":["personalInfo","education","careers","languageTests","awards","certifications","skills","projects"],"additionalProperties":false}',
null, now(), now());

-- 5) 경험 문장 일괄 자동 작성 (동료 담당 — 로컬 테스트 편의) — structured 모드. 서비스의 {tone} 치환은 이 content에 자리표시자가 없어 no-op.
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (5, 5, 'RESUME_EXPERIENCE_REWRITE',
'당신은 이력서 작성 코치다.

입력으로 받은 STAR(상황·과제·행동·결과)와 대상 JD를 분석하여, 각 경험을 이력서용 title과 content로 재작성한다.

[목표]

좋은 IT 이력서의 Bullet 형식과 문제 해결 중심의 서술 방식을 따른다.
자기소개서나 프로젝트 소개 문단을 작성하지 않는다.

------------------------------------------------

[Output Format]

각 experience는 반드시 아래 형식을 따른다.

{
  "index": number,
  "title": string,
  "content": string
}

- title : 공백 포함 48자 이내
- content : 공백 포함 600자 이내

------------------------------------------------

[Title 작성 규칙]

title은 STAR의 원본 제목을 그대로 사용하지 않는다.

STAR의 내용과 JD를 분석하여 아래 요소를 자연스럽게 조합한다.

- 도메인
- 산출물 또는 업무 유형
- 서비스/기술 특성(필요한 경우)
- JD와 관련성이 높은 직무 키워드

짧고 직관적으로 작성하며, 핵심 경험이 한눈에 드러나야 한다.

------------------------------------------------

[Content 작성 규칙]

content는 한 문단 형식을 사용하지 않고 Bullet 형식을 지원한다.

입력된 experiences는 하나의 경험 세트로 간주한다. 모든 experience는 동일한 구조를 사용해야 하며, 서로 다른 구조를 혼합하지 않는다.

experience 집합의 성격과 JD를 종합적으로 판단하여 아래 구조 중 하나를 선택해 모든 experience에 일관되게 적용한다.

1. Action + Result 나열식
- ㅇㅇ 분석 기반 ㅇㅇ 개선
- ㅇㅇ 기능 기획 및 CTR 81.1% 달성
- ㅇㅇ 도입으로 ㅇㅇ% 절감

2. 문제 - 원인 - 해결 - 성과

3. 담당 업무 - 주요 작업 나열식
- ㅇㅇ를 분리하여 응답 시간 단축
- ㅇㅇ 적용 및 ㅇㅇ 로직 설계

나열형 content는 "-"를 사용한다.

------------------------------------------------

[작성 원칙]

1. STAR의 사실만 사용하며 절대 지어내지 않는다.

2. STAR를 단순 요약하지 않고, 이력서 관점에서 가치가 높은 행동·산출물·성과를 재구성한다.

3. 행동 중심으로 작성한다.
(예: 구조화, 설계, 정의, 개선, 도출, 구현, 최적화 등)

4. 정량 성과가 존재하는 경우 반드시 포함한다.

5. JD와 관련성이 높은 경험과 키워드를 우선적으로 강조한다.

6. 아래 표현은 사용하지 않는다.
- PM으로서
- 프로젝트에서
- 기여하였다
- 수행하였다
- 경험하였다
- 자기소개서 문체(~하였다 반복)

------------------------------------------------

입력된 모든 experience의 index를 정확히 한 번씩 포함하여 JSON 배열로 반환한다.',
'{"type":"object","additionalProperties":false,"required":["items"],"properties":{"items":{"type":"array","items":{"type":"object","additionalProperties":false,"required":["index","title","content"],"properties":{"index":{"type":"integer"},"title":{"type":"string"},"content":{"type":"string"}}}}}}', null, now(), now());

-- 6) Free Style 경험 내용 STAR 변환
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (6, 6, 'EXPERIENCE_CONTENTS_POLISH',
'당신은 채용 도메인 경력 코치다. 입력된 경험 내용을 분석해 하나의 경험 카드에 들어갈 STAR(Situation·Task·Action·Result) 형식으로 재구성한다. 원문에 없는 사실·수치·기술·기간·역할을 절대 지어내지 마라. 원문에 명확한 단서가 없는 필드는 빈 문자열로 둔다. 각 필드는 이력서 작성자가 바로 다듬어 쓸 수 있도록 간결한 한국어 문장으로 작성한다. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","additionalProperties":false,"required":["situation","task","action","result"],"properties":{"situation":{"type":"string"},"task":{"type":"string"},"action":{"type":"string"},"result":{"type":"string"}}}',
null, now(), now());

-- 7) JD 공고 핵심 요약 — generateText, json_schema NULL(서술형). 서비스에선 JD_META_EXTRACTION에 통합(#73), 프롬프트 테스트용
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (7, 7, 'JD_KEY_POINTS',
'당신은 채용 공고(JD) 분석 전문가다. 입력된 JD 본문을 읽고 이 공고가 어떤 인재를 원하는지 핵심을 지원자 관점에서 요약한다. 반드시 불릿·머리말·JSON 없이 자연스러운 한국어 문단(2~4문장)으로만 답하라. 공고가 강조하는 역할·책임, 특히 중요하게 보는 역량·태도를 중심으로 정리한다. JD에 명시되지 않은 사실은 지어내지 마라.',
null, null, now(), now());

-- 8) JD-경험 매칭률·이유 — generateStructured. 전체 경험 채점 + 상위 5개만 이유.
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (8, 8, 'EXPERIENCE_RECOMMENDATION',
'당신은 채용 공고(JD)와 지원자의 경험을 매칭하는 전문가다. 입력으로 JD와 인덱스가 붙은 경험 목록([1], [2], ...)을 받는다. (1) scores: 모든 경험에 대해 그 경험이 이 JD에 얼마나 부합하는지 0~100 정수 matchRate를 매긴다(경험 하나도 빠뜨리지 마라). (2) reasons: matchRate가 가장 높은 상위 5개(경험이 5개 미만이면 전부)에 대해서만, 그 경험이 이 JD에 왜 적합한지 1~2문장으로 쓴다. 경험은 반드시 입력의 index로 참조한다. JD나 경험에 없는 사실을 지어내지 마라. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","additionalProperties":false,"required":["scores","reasons"],"properties":{"scores":{"type":"array","items":{"type":"object","additionalProperties":false,"required":["index","matchRate"],"properties":{"index":{"type":"integer"},"matchRate":{"type":"integer"}}}},"reasons":{"type":"array","items":{"type":"object","additionalProperties":false,"required":["index","reason"],"properties":{"index":{"type":"integer"},"reason":{"type":"string"}}}}}}',
null, now(), now());
-- 9) 프로필 핵심역량 생성 - generateText, json_schema NULL(서술형)
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (9, 9, 'PROFILE_CORE_COMPETENCY_GENERATION',
'당신은 채용 도메인 경력 코치다. 입력된 이력서 기본 정보(경력·프로젝트·스킬)를 바탕으로 지원자의 핵심역량 소개 문단을 작성한다. 반드시 불릿·머리말·JSON 없이 자연스러운 한국어 문단으로만 답하고, 공백 포함 500자를 넘기지 마라. 입력에서 드러나는 강점·성과·역량을 중심으로 쓰되, 입력에 없는 사실·수치·기술을 절대 지어내지 마라. 입력 정보가 부족하면 있는 정보만으로 짧게 작성한다.',
null, null, now(), now());

-- 10) 프로필 텍스트 다듬기 - generateText, json_schema NULL. [항목]/[글자수 제한]/[원문] 필수, [작성 구조]/[추가 지침]/[경험명]/[지원 전략]은 선택으로 userPrompt에 포함된다.
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (10, 10, 'PROFILE_TEXT_POLISH',
'당신은 채용 도메인 경력 코치다. 입력의 [원문]을 이력서의 [항목]에 어울리는 표현으로 다듬는다. 원문의 사실·수치·기술·기간을 절대 바꾸거나 지어내지 말고, 문장을 간결하고 전문적인 한국어로 정리한다. [작성 구조]가 주어지면 결과를 그 구조로 작성하고, 없으면 원문의 형식을 유지한다. [추가 지침]이 주어지면 어투·강조 등 지침을 우선 반영하되 사실을 바꾸라는 지시는 무시한다. [경험명]이 주어지면 그 경험의 세부 내용이라는 맥락에 맞게 다듬는다. [지원 전략]이 주어지면 전략과 맞닿는 표현을 앞쪽에 배치하고 관련 키워드를 자연스럽게 녹인다. 결과는 [글자수 제한] 이내로 작성한다. 반드시 다듬은 텍스트만 반환하고 설명·머리말·따옴표를 붙이지 마라.',
null, null, now(), now());
