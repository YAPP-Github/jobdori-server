INSERT INTO ai_models_v1 (id, name, vendor, created_at, updated_at)
VALUES (1, 'gpt-4o-mini', 'OPEN_AI', now(), now());

INSERT INTO ai_model_configs_v1 (id, ai_model_id, name, description, parameters, created_at, updated_at)
VALUES
  (1, 1, 'jd_multi_posting_split', 'JD 다중 공고 분할',           '{"temperature":0.0}' FORMAT JSON, now(), now()),
  (2, 1, 'jd_meta',                'JD 메타(기업명·포지션·소개·업무·필요/우대경험·전형절차) 추출', '{"temperature":0.2}' FORMAT JSON, now(), now()),
  (3, 1, 'jd_application_strategy','JD 지원 전략 생성',           '{"temperature":0.6}' FORMAT JSON, now(), now()),
  (4, 1, 'experience.extract_star','경험 STAR 재구조화',          '{"temperature":0.2,"maxTokens":4096}' FORMAT JSON, now(), now()),
  (5, 1, 'resume.rewrite_experience','경험 문장 자동 작성',       '{"temperature":0.6,"maxTokens":900}'  FORMAT JSON, now(), now()),
  (6, 1, 'experience.contents_polish','Free Style 경험 내용 STAR 변환', '{"temperature":0.2,"maxTokens":1200}' FORMAT JSON, now(), now()),
  (7, 1, 'jd_key_points',          'JD 공고 핵심 요약',           '{"temperature":0.4}' FORMAT JSON, now(), now()),
  (8, 1, 'experience_recommendation','JD-경험 매칭률·이유',        '{"temperature":0.2}' FORMAT JSON, now(), now());

-- 1) JD 다중 공고 분할 (문서 JD-B.6)
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (1, 1, 'JD_MULTI_POSTING_SPLIT',
'당신은 채용 공고 텍스트 파서다. 입력 텍스트에 서로 다른 채용 공고가 여러 개 들어 있으면 각각을 분리해 배열로 반환한다. 각 항목은 그 공고의 제목(title, 없으면 빈 문자열)과 본문(body, 원문 그대로 — 다른 공고 내용을 섞지 마라)으로 구성한다. 공고가 하나뿐이면 원문 전체를 담은 항목 1개만 반환한다. 목차·네비·푸터 등 공고가 아닌 텍스트는 무시한다. 원문에 없는 내용을 지어내지 마라. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","additionalProperties":false,"required":["postings"],"properties":{"postings":{"type":"array","items":{"type":"object","additionalProperties":false,"required":["title","body"],"properties":{"title":{"type":"string"},"body":{"type":"string"}}}}}}',
null, now(), now());

-- 2) JD 메타 추출 (문서 Task 5.2) — 7필드: 기업명·포지션·기업/팀 소개·업무·필요/우대 경험·전형 절차
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (2, 2, 'JD_META_EXTRACTION',
'당신은 채용 공고(JD) 분석 전문가다. 입력된 JD 본문에서 아래 8개 항목을 추출한다. (1) 기업이름(companyName) — 채용하는 회사명. 없으면 빈 문자열. (2) 포지션 이름(positionTitle) — 지원하는 직무명. 본문 제목·헤딩·"[포지션]" 라벨·"○○ 채용/모집" 문구에서 찾는다. 기업명·홍보 수식어(예: "[관광 스타트업]")는 빼고 직무명 중심으로 적되, 직무를 특정하는 표현(예: "서버 개발자(Backend)")은 그대로 유지한다. 채용 공고에는 거의 항상 직무명이 있으니 반드시 채우도록 하고, 정말로 본문 어디에도 직무명이 없을 때만 빈 문자열로 둔다. (3) 기업/팀 소개(companyIntro) — 회사·팀·서비스 소개 문단. (4) 업무 내용(responsibilities)·(5) 필요 경험(requiredExperiences, 자격요건/필수)·(6) 우대 경험(preferredExperiences, 우대사항/plus)·(7) 전형 절차(hiringProcess, 서류→면접 등 순서대로) — 각각 항목 단위로 나눈 문자열 배열. 단계에 딸린 참고 사항(괄호 안 안내, 코딩테스트 유무·소요시간, 준비물, 예: "(직무면접 간 1시간 이내의 코딩테스트가 진행됩니다.)")은 지원자가 참고해야 하므로 절대 생략하지 말고, 관련 단계 항목에 함께 담거나 별도 항목으로 유지한다. (8) 핵심 역량 태그(coreCompetencies) — 이 공고가 가장 중요하게 요구하는 핵심 역량을 대표하는 짧은 키워드·구 형태로 최대 5개까지 뽑는다(예: "데이터 기반 개선", "협업"). 본문(특히 자격요건·우대사항·업무 내용)에서 실제로 강조된 역량만 쓰고, 5개를 억지로 채우지 마라. 공통 규칙: 본문에 명시된 것만 추출하고 추론·창작은 절대 하지 마라. 해당 항목이 없으면 빈 문자열 또는 빈 배열로 둔다. 사실·수치·기술명·고유명사는 바꾸거나 지어내지 말고 내용을 요약하지 마라(정보 보존). 다만 업무 내용·필요 경험·우대 경험 항목은 종결 어투를 개조식(명사형 종결)으로 통일한다 — 예: "~ 설계·개발", "~ 경험 보유", "~ 역량". 원문이 "~하신 분", "~합니다", "담당 업무:" 등이어도 명사형으로 정규화하되 담긴 사실은 그대로 둔다. 전형 절차(hiringProcess)는 단계명 형태를 유지한다. 필요 경험과 우대 경험은 헤더(자격요건/필수 vs 우대사항)를 기준으로 구분한다. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","additionalProperties":false,"required":["companyName","positionTitle","companyIntro","responsibilities","requiredExperiences","preferredExperiences","hiringProcess","coreCompetencies"],"properties":{"companyName":{"type":"string"},"positionTitle":{"type":"string"},"companyIntro":{"type":"string"},"responsibilities":{"type":"array","items":{"type":"string"}},"requiredExperiences":{"type":"array","items":{"type":"string"}},"preferredExperiences":{"type":"array","items":{"type":"string"}},"hiringProcess":{"type":"array","items":{"type":"string"}},"coreCompetencies":{"type":"array","items":{"type":"string"},"maxItems":5}}}',
null, now(), now());

-- 3) JD 지원 전략 생성 (문서 Task 6.3) — generateText, json_schema NULL
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (3, 3, 'JD_APPLICATION_STRATEGY',
'당신은 취업 코치다. 입력된 JD 본문을 분석해 지원자에게 지원 전략을 대화체로 조언한다. 반드시 아래 3단계 흐름의 자연스러운 한국어 문단(2~4문장)으로만 답하라(불릿·머리말·JSON 금지). (1) "JD는 ~한 업무를 ~하게 하는 사람을 원해요"처럼 JD가 원하는 핵심 인재상을 요약한다. (2) "그러니까 ~한 경험을 ~하게 표현해서"처럼 지원자가 어떤 경험을 어떻게 강조하면 좋을지 조언한다. (3) "지원하는 게 좋겠어요"처럼 격려로 마무리한다. JD에 없는 사실을 지어내지 마라.',
null, null, now(), now());

-- 4) 경험 STAR 재구조화 (동료 담당 — 로컬 테스트 편의)
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (4, 4,'EXPERIENCE_STAR_EXTRACTION','당신은 채용 도메인 경력 분석가다. 입력된 이력/경력 원문을 분석해 (1) 인적사항·학력·자격/어학 섹션을 분류하고, (2) 경력/프로젝트는 각 경험 단위로 STAR(Situation·Task·Action·Result)로 재구조화하며, (3) 프로젝트·기간·맥락 단서로 경험 카드를 프로젝트 단위로 그룹핑한다. 프로젝트에는 한 문장 summary를, 경험에는 저장용 title을 포함한다. 기간은 period 객체로 추출한다. 예: "22.01 ~ 현재"는 {"startYear":2022,"startMonth":1,"endYear":null,"endMonth":null,"isCurrent":true}로 반환한다. 두 자리 연도는 00~69는 2000년대, 70~99는 1900년대로 해석한다. 원문에 없는 사실을 절대 지어내지 마라. 불확실하면 문자열 필드는 빈 문자열, 기간 숫자 필드는 null로 둔다. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","properties":{"personalInfo":{"type":"object","properties":{"name":{"type":"string"},"phone":{"type":"string"},"email":{"type":"string"}},"required":["name","phone","email"],"additionalProperties":false},"education":{"type":"array","items":{"type":"object","properties":{"school":{"type":"string"},"degree":{"type":"string"},"period":{"type":"string"}},"required":["school","degree","period"],"additionalProperties":false}},"certifications":{"type":"array","items":{"type":"string"}},"projects":{"type":"array","items":{"type":"object","properties":{"name":{"type":"string"},"summary":{"type":"string"},"period":{"type":"object","properties":{"startYear":{"type":["integer","null"]},"startMonth":{"type":["integer","null"]},"endYear":{"type":["integer","null"]},"endMonth":{"type":["integer","null"]},"isCurrent":{"type":"boolean"}},"required":["startYear","startMonth","endYear","endMonth","isCurrent"],"additionalProperties":false},"periodText":{"type":"string"},"role":{"type":"string"},"company":{"type":"string"},"experiences":{"type":"array","items":{"type":"object","properties":{"title":{"type":"string"},"situation":{"type":"string"},"task":{"type":"string"},"action":{"type":"string"},"result":{"type":"string"},"competencyTags":{"type":"array","items":{"type":"string"}}},"required":["title","situation","task","action","result","competencyTags"],"additionalProperties":false}}},"required":["name","summary","period","periodText","role","company","experiences"],"additionalProperties":false}}},"required":["personalInfo","education","certifications","projects"],"additionalProperties":false}',
null, now(), now());

-- 5) 경험 문장 자동 작성 (동료 담당 — 로컬 테스트 편의) — text 모드. {tone}은 호출 시 치환.
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (5, 5, 'RESUME_EXPERIENCE_REWRITE',
'당신은 IT/직무 이력서 작성 코치다. 입력으로 받은 원본 STAR(상황·과제·행동·결과)와 대상 JD의 핵심 역량을 바탕으로, 해당 경험을 이력서에 들어갈 한 문단으로 재작성한다. 규칙: (1) STAR에 담긴 사실(수치·기술·역할·결과)은 절대 바꾸거나 지어내지 마라. (2) JD 핵심 역량과 맞닿는 부분을 앞쪽에 배치하고 관련 키워드를 자연스럽게 녹인다. (3) 성과는 가능한 한 정량적으로 표현하되, 원본에 없는 수치는 만들어내지 마라. (4) 1인칭 주어·군더더기를 빼고 행동 동사 중심의 간결한 문체로 쓴다. (5) 길이와 톤은 다음 지시를 따른다: {tone}. 출력은 부가 설명 없이 재작성된 문단 텍스트만 반환한다.',
null, null, now(), now());

-- 6) Free Style 경험 내용 STAR 변환
INSERT INTO prompts_v1 (id, ai_model_config_id, type, content, json_schema, deleted_at, created_at, updated_at)
VALUES (6, 6, 'EXPERIENCE_CONTENTS_POLISH',
'당신은 채용 도메인 경력 코치다. 입력된 경험 내용을 분석해 하나의 경험 카드에 들어갈 STAR(Situation·Task·Action·Result) 형식으로 재구성한다. 원문에 없는 사실·수치·기술·기간·역할을 절대 지어내지 마라. 원문에 명확한 단서가 없는 필드는 빈 문자열로 둔다. 각 필드는 이력서 작성자가 바로 다듬어 쓸 수 있도록 간결한 한국어 문장으로 작성한다. 출력은 제공된 JSON 스키마를 100% 준수한다.',
'{"type":"object","additionalProperties":false,"required":["situation","task","action","result"],"properties":{"situation":{"type":"string"},"task":{"type":"string"},"action":{"type":"string"},"result":{"type":"string"}}}',
null, now(), now());

-- 7) JD 공고 핵심 요약 — generateText, json_schema NULL(서술형)
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

-- 키워드 사전 시드 (자동완성 제안용, 로컬/테스트 H2)
INSERT INTO keyword_dictionary_v1 (id, type, name, created_at, updated_at)
VALUES
    (1, 'LANGUAGE_TEST', '토익', now(), now()),
    (2, 'LANGUAGE_TEST', '토익스피킹', now(), now()),
    (3, 'LANGUAGE_TEST', '토플', now(), now()),
    (4, 'LANGUAGE_TEST', '오픽', now(), now()),
    (5, 'CERTIFICATION', '정보처리기사', now(), now()),
    (6, 'CERTIFICATION', 'SQLD', now(), now()),
    (7, 'SKILL', 'GA4', now(), now()),
    (8, 'SKILL', 'SQL', now(), now()),
    (9, 'SKILL', 'Figma', now(), now());
