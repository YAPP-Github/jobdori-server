# 프롬프트 평가 파이프라인

이 파이프라인은 Scoop 백엔드의 14개 AI 프롬프트를 골든셋으로 평가한다. 대상은 JD_MULTI_POSTING_SPLIT, JD_META_EXTRACTION, JD_APPLICATION_STRATEGY, EXPERIENCE_STAR_EXTRACTION, RESUME_EXPERIENCE_REWRITE, EXPERIENCE_CONTENTS_POLISH, EXPERIENCE_RECOMMENDATION, PROFILE_CORE_COMPETENCY_GENERATION, PROFILE_CORE_COMPETENCY_JD_TEMPLATE, PROFILE_TEXT_POLISH, EXPERIENCE_PROJECT_DUPLICATE_MATCH, EXPERIENCE_DUPLICATE_MERGE, EXPERIENCE_QUESTION_ROOM_GENERATION, EXPERIENCE_QUESTION_ROOM_CHAT이다. DOCUMENT_TEXT_EXTRACTION과 JD_KEY_POINTS는 대상이 아니다.

## 사전 준비

eval 디렉터리에서 `npm install`을 실행한다. `OPENAI_API_KEY`를 설정하고, 덤프할 때는 PGHOST, PGPORT, PGUSER, PGPASSWORD, PGDATABASE를 psql 표준 환경변수로 설정한다.

## 프롬프트 스냅샷

`npm run dump`로 DB의 현재 프롬프트를 `prompts.json`에 저장한다. 이 파일은 prompt_id와 prompt_updated_at을 포함한 스냅샷이라 어느 프롬프트 버전의 결과인지 추적할 수 있어 커밋한다. 커밋된 스냅샷은 **로컬 DB 기준**이며 14종 전부를 담고 있다. 운영에 아직 없는 프롬프트(EXPERIENCE_QUESTION_ROOM_GENERATION, EXPERIENCE_QUESTION_ROOM_CHAT, PROFILE_CORE_COMPETENCY_JD_TEMPLATE)와 운영보다 앞선 개선본을 배포 전에 평가하기 위해서다. 모델과 파라미터는 운영 DB와 같고, JD_META_EXTRACTION, JD_APPLICATION_STRATEGY, EXPERIENCE_STAR_EXTRACTION, RESUME_EXPERIENCE_REWRITE, EXPERIENCE_CONTENTS_POLISH, EXPERIENCE_RECOMMENDATION 6종은 프롬프트 본문이 운영과 다르다. 운영 버전을 평가하려면 운영 DB 접속 정보로 덤프를 다시 뜬다. 프롬프트를 배포한 뒤에는 덤프를 다시 떠서 스냅샷을 갱신한다. 갱신하지 않으면 예전 버전을 평가하게 된다.

접속 정보는 psql 표준 환경변수로 넘긴다. 파일에는 프롬프트 본문, json_schema, 모델명, 파라미터, 버전만 들어가고 접속 정보는 저장되지 않는다.

평가에 쓰는 모델과 파라미터는 전부 이 스냅샷에서 온다. **모델과 파라미터 모두 운영 DB 값을 정본으로 확정했다.** 기획 문서 0장 표와 어긋나는 곳이 있지만 DB를 따른다. 바꾸려면 DB의 ai_model_configs_v1을 고치고 덤프를 다시 뜬다. prompts.json을 직접 고치지 않는다.

| PromptType | 기획 문서 표 | DB (정본) |
|---|---|---|
| EXPERIENCE_RECOMMENDATION | gpt-4o, temperature 0.4 | gpt-4o-mini, temperature 0.2 |
| PROFILE_CORE_COMPETENCY_GENERATION | gpt-4o | gpt-4o-mini, maxTokens 900 |
| RESUME_EXPERIENCE_REWRITE | maxTokens 미기재 | maxTokens 900 |

나머지 8개 타입은 문서와 DB가 일치한다. 결과적으로 11개 타입 모두 gpt-4o-mini다. EXPERIENCE_QUESTION_ROOM_GENERATION과 EXPERIENCE_QUESTION_ROOM_CHAT도 gpt-4o-mini이고 temperature는 각각 0.2, 0.3, maxTokens는 둘 다 2048이다.

## 평가 실행

평가할 타입과 골든셋 위치를 같이 준다. 둘 다 필수다.

```
GOLDENSET=https://docs.google.com/spreadsheets/d/SHEET_ID/edit PROMPT_TYPE=JD_META_EXTRACTION npm run eval
GOLDENSET=~/Downloads/JD_META_EXTRACTION.csv PROMPT_TYPE=JD_META_EXTRACTION npm run eval
GOLDENSET=<위와 동일> PROMPT_TYPE=JD_META_EXTRACTION npm run eval -- --filter-first-n 2
```

`npm run eval`은 `prompts.json`에서 해당 타입의 모델명과 프롬프트 버전을 읽어 MODEL_NAME, PROMPT_ID, PROMPT_UPDATED_AT으로 넘긴 뒤 promptfoo를 실행한다. 그래서 결과 표의 프롬프트 라벨에 `JD_META_EXTRACTION prompt=2@2026-07-30T12:41:22.137592 model=gpt-4o-mini` 형태로 버전이 함께 남는다. promptfoo 옵션은 `--` 뒤에 그대로 이어 붙일 수 있다.

루브릭은 `rubrics/_common.yaml`과 `rubrics/<PROMPT_TYPE>.yaml`에서 읽는다. 골든셋은 저장소에 없고 GOLDENSET으로 받는다. 비공개 시트는 서비스 계정 자격증명(GOOGLE_APPLICATION_CREDENTIALS)과 뷰어 권한이 필요하다. 첫 실행은 `--filter-first-n 2`로 앞 2건만 돌려 설정과 권한이 맞는지 확인한 뒤 전체를 돌린다. 케이스마다 대상 모델과 judge를 각각 호출하므로(PROFILE_TEXT_POLISH는 pairwise까지 3회) 설정이 틀린 채 전량을 돌리면 비용만 나간다.

release 임계치는 아직 정하지 않았으므로 어떤 값도 걸려 있지 않다.

## 채점

judge는 평가 대상과 별도의 모델로 호출한다. 기본값은 gpt-4o이고 `JUDGE_MODEL`로 바꿀 수 있다. 대상 14종이 모두 gpt-4o-mini이므로 judge가 한 급 위라는 설계서 조건을 전 타입에서 만족한다. 나중에 어떤 타입을 gpt-4o로 올리면 그 타입은 judge도 함께 올려야 한다. temperature 0, 구조화 출력 강제이며, 한 번의 호출로 그 타입의 모든 축을 채점한다. 대상과 같은 모델로 자기 출력을 채점하면 self-preference bias가 붙기 때문에 대상 provider를 재사용하지 않는다.

축의 tier(gate/core/diag)는 judge 응답이 아니라 루브릭 파일을 신뢰한다. judge가 축을 빠뜨리면 그 케이스는 조용히 통과하지 않고 에러로 끝난다.

### 계산으로 채점하는 축

루브릭 축에 `scoring` 키가 붙어 있으면 judge 주관 채점 대신 계산으로 점수를 낸다. 그 축은 judge 프롬프트에서 아예 빠지므로 judge가 같은 축을 중복 채점하지 않는다. 정답 metadata가 골든셋에 없으면 계산할 수 없으므로 그 축만 judge 채점으로 되돌아간다. 즉 정답을 채우기 전에도 파이프라인은 그대로 돈다.

| 축 | scoring | 필요한 골든셋 컬럼 | 계산 방식 |
|---|---|---|---|
| EXPERIENCE_RECOMMENDATION 순위타당성 | kendall_tau | `__metadata:expected_ranking` | matchRate 내림차순과 정답 순위의 Kendall tau-b |
| EXPERIENCE_PROJECT_DUPLICATE_MATCH 판정정확도 | precision | `__metadata:expected_matches` | 모델이 중복이라 붙인 건 중 정답과 다른 대상에 붙인 비율 |
| EXPERIENCE_DUPLICATE_MERGE 판정정확도 | precision | `__metadata:expected_matches` | 위와 같음 |
| PROFILE_TEXT_POLISH 개선실재성 | pairwise | `__metadata:original` | 원문과 첨삭본을 순서를 뒤집어 두 번 비교 |

점수 변환은 이렇다.

- Kendall tau-b: 0.8 이상 5점, 0.6 이상 4점, 0.4 이상 3점, 0.2 이상 2점, 그 미만 1점. 임계값은 baseline을 보고 조정한다(metrics.js).
- precision: 오병합이 한 건도 없으면 5점, 한 건이라도 있으면 1점. 게이트 축이라 오병합 1건이 곧 배포 차단이다.
- pairwise: 순서를 바꿔도 첨삭본이 낫다고 나오면 5점, 판정이 뒤집히면 위치 편향으로 보고 3점, 순서를 바꿔도 원문이 낫다고 나오면 1점. 호출이 케이스당 2회 늘어난다.

RESUME_EXPERIENCE_REWRITE 본문축도 같은 pairwise 패턴을 쓸 수 있다. 기획 문서가 검토 항목으로 남겨둔 상태라 기본은 꺼져 있고, 켜려면 `rubrics/RESUME_EXPERIENCE_REWRITE.yaml`의 본문축에 `scoring: pairwise` 한 줄을 넣고 골든셋에 `__metadata:original`을 채우면 된다.

## Google Sheets 공유

두 가지 형태가 있다. 축별 점수까지 보여줄 거면 아래 스코어카드를 쓴다.

### 기본 출력 (케이스당 한 칸)

promptfoo 내장 Sheets 출력을 사용한다. 스프레드시트 URL을 output으로 주고 Google 서비스 계정 자격증명 경로를 설정한다. 서비스 계정 이메일에 해당 스프레드시트 편집 권한을 부여해야 한다.

`GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json PROMPT_TYPE=JD_META_EXTRACTION npm run eval -- --output https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/edit`

### 스코어카드 (축별로 펼치기)

기본 출력은 케이스당 한 칸이라 어느 축에서 깎였는지 보이지 않는다. 결과 JSON을 축별 점수판으로 펼쳐 시트에 쓴다.

```
GOLDENSET=<시트 URL> PROMPT_TYPE=JD_META_EXTRACTION npm run eval -- --output run.json
npm run scorecard -- run.json --sheet https://docs.google.com/spreadsheets/d/SHEET_ID/edit
```

`--sheet` 없이 `--csv 경로`만 주면 CSV로 떨어진다(둘 다 줘도 된다). 아무것도 안 주면 결과 파일 옆에 `<이름>-scorecard.csv`를 만든다. URL에 `gid`가 있으면 그 탭에, 없으면 첫 탭에 쓴다. **탭 내용을 덮어쓰므로** 실행마다 탭을 나누거나 시트를 따로 두는 편이 안전하다.

인증은 promptfoo와 같다. `GOOGLE_APPLICATION_CREDENTIALS`에 서비스 계정 키를 두고, 그 계정 이메일에 편집 권한을 준다.

열은 이렇게 나온다.

| 열 | 내용 |
|---|---|
| 케이스 | 골든셋의 case_id |
| 노린 실패 유형 | 골든셋의 note |
| 입력 | 그 케이스의 user 프롬프트 전문 |
| 판정 | 통과 / 실패 / 에러. 에러는 모델 호출이나 채점이 실패한 것으로 프롬프트 품질과 무관하다 |
| 핵심 평균 | core 축 산술 평균 |
| `<축 이름> [게이트/핵심/진단]` | 축마다 한 열, 1-5 점수 |
| 판정 근거 | 축별 judge 근거 또는 계산 결과를 줄바꿈으로 이어 붙인 것. 에러 행은 그 사유가 들어간다 |
| 모델, 프롬프트 버전 | 어느 버전을 잰 결과인지 |

### 케이스별 요약 (반복 실행 통과율)

temperature가 0보다 큰 프롬프트는 같은 입력에서도 실행마다 판정이 뒤집힌다. 한 번 돌린 통과/실패로 프롬프트 개선 여부를 판단하지 말고 `--repeat`로 여러 번 돌려 통과율로 본다.

```
GOLDENSET=<시트 URL> PROMPT_TYPE=PROFILE_CORE_COMPETENCY_GENERATION npm run eval -- --repeat 3 --output run.json
npm run scorecard -- run.json --csv run.csv
```

스코어카드를 만들 때 케이스별 요약이 터미널에 표로 찍히고, `--csv`를 주면 `<이름>-summary.csv`가 함께 생긴다. 시트로 보내려면 `--summary-sheet <URL>`을 준다. 상세 시트와 다른 탭(`gid`)을 지정해야 서로 덮어쓰지 않는다.

| 열 | 내용 |
|---|---|
| 실행 수 | 그 케이스를 돌린 횟수 |
| 통과율 | 통과 수 / (실행 수 - 에러) |
| 에러 | 모델 호출이나 채점이 실패한 횟수. 통과율 분모에서 뺀다 |
| 게이트 실패 | 게이트 축이 1점이 나온 횟수 |
| 룰 실패 | `__expected` 룰이 하나라도 실패한 횟수 |
| 핵심 평균 | 실행별 핵심 축 평균을 다시 평균한 값 |
| `<축 이름> [게이트/핵심/진단]` | 축별 평균 점수 |

게이트 실패와 룰 실패를 따로 세는 이유는 원인이 달라서다. 게이트 실패는 judge가 본 내용 문제(환각 등)이고, 룰 실패는 프롬프트가 명시한 형식 규칙(금지 표현, 글자 수 등) 위반이다.

## 결과 읽기

판정이 "에러"인 행은 모델 호출이나 채점이 실패한 것이라 프롬프트 품질과 무관하다. 축 점수와 핵심 평균이 비어 있으므로 실패율을 셀 때 빼야 한다. gate는 1점이면 해당 케이스가 즉시 fail이다. 배포를 막는 신호이지만 실제로 막는 CI 잡은 아직 없다. core는 release 임계치 적용 대상이고 임계치는 첫 baseline 결과를 보고 정한다. diag는 배포를 막지 않고 회귀 원인을 분해할 때 쓴다. 케이스 점수는 가중치 없이 core 축의 산술 평균(1-5)이고, 축별 점수와 근거는 componentResults에 축 이름과 tier가 붙어 남는다.

## 골든셋 (PM 인계용)

골든셋은 이 저장소에 두지 않는다. Google Sheets 또는 로컬 파일로 관리하고 실행할 때 `GOLDENSET`으로 넘긴다. 시트를 쓰면 PM이 고치는 즉시 다음 실행에 반영되고, 프롬프트 저장소에 케이스 데이터가 섞이지 않는다.

### 시트 첫 줄에 그대로 붙여넣을 헤더

```
case_id	input	note	__expected	__metadata:expected
```

밑줄 2개(`__`)로 시작하는 열은 promptfoo 예약어다. 이름이 하나라도 틀리면 그 열은 통째로 무시된다(단, 밑줄을 1개만 쓰면 실행 시 경고가 뜬다).

| 열 | 필수 | 내용 |
|---|---|---|
| `case_id` | O | 짧은 식별자. 예: meta-01 |
| `input` | O | 서비스가 실제로 보내는 user 프롬프트 전문 |
| `note` | O | 이 케이스가 노리는 실패 유형 한 줄 |
| `__expected` | X | 기계가 자동으로 검사할 정답. 비우면 검사하지 않는다 |
| `__metadata:expected` | X | judge에게 정답으로 알려줄 설명. 비우면 judge가 입력과 출력만 보고 채점한다 |

계산 채점을 쓰는 타입은 열이 하나 더 붙는다. 형식을 지켜야 계산이 돈다.

| 열 | 대상 타입 | 형식 | 예 |
|---|---|---|---|
| `__metadata:expected_ranking` | EXPERIENCE_RECOMMENDATION | 좋은 순서대로 경험 index를 쉼표로. 동점은 등호로 묶는다 | `3,1=2,5,4` |
| `__metadata:expected_matches` | EXPERIENCE_PROJECT_DUPLICATE_MATCH, EXPERIENCE_DUPLICATE_MERGE | `index:정답id` 쌍. 중복이 아니면 null | `1:12,2:null,3:7` |
| `__metadata:original` | PROFILE_TEXT_POLISH | 첨삭 전 원문 그대로 | `저는 열심히 일했습니다.` |

타입마다 시트를 따로 두고, 실행할 때 그 시트 URL을 `GOLDENSET`으로 넘긴다. 한 시트의 여러 탭을 쓰려면 URL에 `gid`까지 붙인다.

### input 형식

system 프롬프트는 DB 실물을 쓰므로 input도 서비스가 만드는 형식과 같아야 한다. 형식이 다르면 실제로 발생하지 않는 조합을 평가하게 된다. 각 타입의 형식은 해당 서비스의 buildUserPrompt를 따른다. 예를 들어 RESUME_EXPERIENCE_REWRITE는 `## 대상 JD` 아래 회사/포지션/주요 업무/필요 경험/우대 경험/핵심 역량/공고 핵심/지원 전략, `## 첨삭할 경험 contents 목록` 아래 `[1]`, `[2]` 번호 형식이다(ResumeExperiencePolishService.kt). PROFILE_CORE_COMPETENCY_JD_TEMPLATE은 JdPromptText 형식(`[기업명]`, `[포지션]`, `[기업/팀 소개]` 한 줄씩, `[업무 내용]`, `[필요 경험]`, `[우대 경험]`, `[전형 절차]`는 `- ` 목록이고 비면 빠짐)이다(ProfileAiService.generateCoreCompetency). EXPERIENCE_QUESTION_ROOM_GENERATION은 `## 담당업무`, `## 우대사항` 아래 `[1]`, `[2]` 번호 형식이고 비어 있는 섹션은 빠진다(GetExperienceQuestionRoomsService.kt). 모델은 sourceType과 index만 내고 sourceText는 서버가 붙이므로, eval에서는 transform.js가 input의 `[n]` 항목으로 sourceText를 붙인다. 없는 번호는 서버와 달리 카드를 버리지 않고 null로 남겨 judge가 보게 한다. EXPERIENCE_QUESTION_ROOM_CHAT은 `## JD`, `## 질문 카드`, `## 선택한 경험`, `## 보충된 정보`, `## 현재 블록`, `## 이번 사용자 입력` 순서다. 프롬프트의 input_description을 따른 형식이고, 아직 서비스(ExperienceQuestionRoomChatService.kt)는 `## 이전 대화`로 보내므로 서비스를 바꿀 때 이 헤더와 맞춘다. 첫 턴은 `## 보충된 정보`와 `## 현재 블록`을 빼고, 형식 규칙은 `## 현재 블록`이 없으면 첫 생성으로 본다. 줄바꿈은 시트 셀 안에서 실제 줄바꿈(alt+enter)으로 넣는다. 문자 그대로의 백슬래시 n은 그 두 글자가 모델에 전달된다.

### `__expected` 쓰는 법

값 하나가 검사 한 줄이다.

- `contains: 브라이트랩` - 출력에 이 문자열이 있어야 한다
- `javascript: (typeof output === "string" ? JSON.parse(output) : output).isJobPosting === false` - 구조화 출력의 특정 필드를 검사한다
- `is-json` - JSON 형식인지만 본다

검사를 두 개 이상 걸려면 `__expected2`, `__expected3` 열을 늘린다.

프롬프트가 모든 출력에 요구하는 형식 규칙은 케이스마다 적지 않고 `rules.js`에 타입별로 둔다. 모든 케이스에 자동으로 걸리고 스코어카드에서 룰 실패로 집계된다. EXPERIENCE_QUESTION_ROOM_CHAT과 PROFILE_CORE_COMPETENCY_JD_TEMPLATE에 있다. PROFILE_CORE_COMPETENCY_JD_TEMPLATE은 프롬프트의 최종 검증 목록(문단 하나, 4~5문장, 헤드라인이 `[[이름]]입니다`로 끝남, 문장마다 자리표시자 1개와 예시 2개, 자리표시자 내용 공백 제외 15자, 예시 제외 400자, 접속 표현/경험 단정/포부 표현/수치/기업명 금지)을 검사한다. EXPERIENCE_QUESTION_ROOM_CHAT은 제목 35자 이하와 "~경험" 제목 금지, 본문 문장마다 마침표 없는 개조식 종결과 한 문장, 범용 동사 금지, fit 한 문장, 첫 생성이면 changed 빈 문자열이고 아니면 한 문장, status와 question 일치를 검사한다.

### 타입별로 무엇을 정답으로 적을지

| PromptType | 권장 정답 |
|---|---|
| JD_MULTI_POSTING_SPLIT | 정답 공고 수와 줄 범위 |
| JD_META_EXTRACTION | 핵심 필드값 몇 개와 비어야 할 필드 |
| EXPERIENCE_RECOMMENDATION | 사람이 매긴 정답 순위 |
| EXPERIENCE_STAR_EXTRACTION, EXPERIENCE_CONTENTS_POLISH | S/T/A/R 귀속 정답, 비어야 할 칸 |
| EXPERIENCE_PROJECT_DUPLICATE_MATCH, EXPERIENCE_DUPLICATE_MERGE | 중복 여부 정답과 병합 시 살아남아야 할 필드 |
| RESUME_EXPERIENCE_REWRITE | 유지해야 할 수치와 항목 수 |
| EXPERIENCE_QUESTION_ROOM_GENERATION | 질문으로 다뤄야 할 핵심 항목. 질문 형식 규칙("경험이 있나요?"로 끝남, "및"/"그리고"로 행동을 묶지 않음)은 `__expected`의 javascript 검사로 건다 |
| EXPERIENCE_QUESTION_ROOM_CHAT | 블록에 들어가야 할 사실과 들어가면 안 되는 사실. bullets 앞 하이픈 금지는 `javascript: !(typeof output === "string" ? JSON.parse(output) : output).block.bullets.some((b) => b.trim().startsWith("-"))`로 검사한다 |
| JD_APPLICATION_STRATEGY, PROFILE_CORE_COMPETENCY_GENERATION, PROFILE_TEXT_POLISH | 정답이 하나로 안 떨어지므로 비워두고 judge에 맡긴다 |

### 작성 예시 (JD_META_EXTRACTION)

| case_id | note | __expected | __metadata:expected |
|---|---|---|---|
| meta-01 | 정상 공고에 사이드바의 다른 공고가 섞인 입력 | `contains: 브라이트랩` | companyName=브라이트랩, positionTitle=백엔드 엔지니어. 사이드바의 프론트엔드 공고는 어느 필드에도 들어가면 안 된다. |
| meta-03 | 채용 공고가 아닌 페이지를 공고로 오인하기 쉬운 입력 | `javascript: (typeof output === "string" ? JSON.parse(output) : output).isJobPosting === false` | isJobPosting=false. 나머지 필드는 비어야 한다. |

### 케이스 수

타입마다 20개에서 30개를 목표로 하고, 프롬프트가 실패하기 쉬운 입력을 절반 이상 넣는다. 잘 되는 케이스만 모으면 회귀가 안 잡힌다.

## 합의도 점검

같은 케이스를 두 번 채점해 점수가 2점 이상 벌어지는 축을 찾는다. 그런 축은 모델이 나빠서가 아니라 앵커가 모호한 것이므로, 점수를 쓰지 말고 앵커 문장부터 고친다(설계서 04장).

```
GOLDENSET=<시트 URL 또는 파일> PROMPT_TYPE=JD_META_EXTRACTION npm run agreement
```

축별로 케이스 수, 2점 이상 흔들린 케이스 수, 최대 흔들림 폭, 가장 심한 케이스를 표로 찍고, 문제 축이 있으면 이름을 모아 알려준다.

- 채점 횟수는 `AGREEMENT_RUNS`로 바꾼다(기본 2).
- judge 캐시를 끄고 돌린다. 켜져 있으면 두 번째 채점이 첫 번째 응답을 그대로 받아 흔들림이 항상 0으로 보인다. `--no-cache`는 대상 모델 호출만 막고 judge 호출은 계속 캐시되므로 이 스크립트가 `PROMPTFOO_CACHE_ENABLED=false`를 직접 건다.
- judge temperature가 0이라 흔들림이 작게 나오는 것이 정상이다. 흔들림 0이 곧 앵커가 좋다는 증거는 아니다. 앵커의 모호함을 더 세게 떠보려면 `JUDGE_TEMPERATURE=0.7`을 앞에 붙여 온도를 올려 돌린다. 평소 채점은 0을 유지한다.
- 대상 모델 호출도 반복되므로 비용이 실행 횟수만큼 늘어난다. 점검 주기와 샘플 수는 아직 정해지지 않았다.

## 워크플로우

설계서 08장의 흐름이다.

1. 프롬프트 버전을 DB에 배포하고 `npm run dump`로 스냅샷을 갱신한다.
2. 골든셋 배치 채점을 돌린다(`GOLDENSET=... npm run eval -- --output run.json`). 룰 축은 자동, 나머지는 judge 1회 호출. 기획과 공유할 거면 `npm run scorecard -- run.json --sheet <URL>`로 축별 점수판까지 만든다.
3. 게이트 fail이거나 핵심축이 임계치에 못 미치면 fail로 본다.
4. fail이면 진단축을 보고 원인이 되는 축을 찾아 프롬프트를 고친 뒤 1번으로 돌아간다. 재검증은 `npm run eval -- --filter-failing run.json`으로 실패했던 케이스만 먼저 돌려 확인하고, 고친 프롬프트를 확정하기 전에 전량을 한 번 돌려 다른 케이스가 나빠지지 않았는지 본다.
5. pass면 배포를 유지하고 실사용 로그를 모니터링한다. 주기적으로 2번을 다시 돌려 재검증한다.

## 온/오프라인 지표가 어긋날 때 (triage)

골든셋 통과율은 안정적인데 실사용 지표(첨삭 재시도 횟수, 이탈 세그먼트 비율 등)만 나빠지는 시점이 이 절차를 트리거하는 신호다. 실제로 문제가 된 실사용 입력을 골든셋 형식으로 만들어 오프라인 루브릭으로 재채점한 뒤 아래로 나눈다.

| 재채점 결과 | 의미 | 조치 |
|---|---|---|
| 루브릭도 fail을 낸다 | 골든셋이 이 입력 패턴을 갖고 있지 않았을 뿐이다 | 골든셋에 케이스 추가. 재현되면 그때 프롬프트를 고친다 |
| 루브릭은 pass인데 유저는 거부했다 | 축 자체가 이 실패 유형을 못 잡는다 | 루브릭 앵커 수정 후 골든셋 라벨 재작업 |
| AI 출력과 무관하다 | UX/기능 이슈다 | 이 파이프라인 대상이 아니므로 별도 이슈로 분리 |

한 가지 더. 앞단 프롬프트의 오류가 뒷단 생성형 프롬프트로 전이되는 도미노 구조가 있다. `JD_META_EXTRACTION`이나 `EXPERIENCE_RECOMMENDATION`이 잘못 뽑으면 그 결과를 입력으로 쓰는 `JD_APPLICATION_STRATEGY`, `RESUME_EXPERIENCE_REWRITE`가 같이 나빠진다. 그러므로 문제로 보이는 프롬프트 하나만 보지 말고, 그 입력을 만든 앞단 프롬프트의 판정 축도 함께 확인한다.

## 루브릭이 설계서와 다른 곳

설계서 07장이 룰 채점을 전제한 두 문장을 뺐다. 기획과 대조할 때 이 절을 근거로 삼는다.

| 축 | 설계서 원문 | 지금 | 이유 |
|---|---|---|---|
| RESUME_EXPERIENCE_REWRITE 본문축 | "불렛 형식 여부는 룰로 함께 검사", 1점 = "불렛 구조가 아니거나(룰로 즉시 감지)" | 세 구조 중 하나를 모든 경험에 일관 적용했는지로 본다. 1점 = 한 문단으로 뭉뚱그렸거나 경험마다 구조가 뒤섞였다 | 배포된 프롬프트는 Action + Result 나열식, 문제 - 원인 - 해결 - 성과, 담당 업무 나열식 세 구조를 허용하고 "-"는 나열형에만 요구한다. 전역 불렛 룰을 걸면 두 번째 구조를 올바르게 따른 출력이 fail한다 |
| PROFILE_TEXT_POLISH 요청사항반영도 | "구조 지정형 요청은 출력에 마커 존재 여부로 룰 채점 대체 가능" | 판정 질문에서 뺐다. 그런 케이스는 골든셋 `__expected`에 검사를 적는다 | 요청 내용이 케이스마다 달라 전역 룰이 성립하지 않는다. 5/3/1 앵커는 설계서 원문 그대로 유지 |

두 축 모두 5/3/1 앵커의 3점은 설계서 원문 그대로다.

## 알려진 제약

- RESUME_EXPERIENCE_REWRITE 본문축의 위치편향 제거는 기본 비활성이다. 켜는 방법은 위 계산 축 절을 본다.
- 시스템 프롬프트에 `{tone}` 같은 런타임 치환 자리가 들어오면 치환하지 않고 그대로 평가한다. 현재 DB 프롬프트에는 그런 자리가 없다.
- maxTokens가 지정되지 않은 타입은 16384로 채워 보낸다. promptfoo가 미지정 시 1024를 강제로 넣어 출력이 잘리기 때문이다. 운영은 max_tokens를 아예 보내지 않는다.
