package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.common.json.JsonUtils
import com.jobdori.core.application.ai.command.AiGenerationRequest
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

/**
 * docs/ai-feature-prompt-design.md §3 기능별 프롬프트·I/O 계약 테스트.
 *
 * 문서에 정의된 (1) System 프롬프트가 요청에 그대로 실리고,
 * (2) 문서의 출력 JSON 스키마 형태로 온 응답이 매칭 도메인 타입으로 역직렬화되는지 검증한다.
 * (MockWebServer는 우리가 넣은 응답을 돌려주므로 "프롬프트 품질"이 아니라 "I/O 계약"을 검증한다.)
 */
class OpenAiPromptContractTest : StringSpec() {

    private lateinit var server: MockWebServer
    private lateinit var chat: OpenAiChatClientImpl

    init {
        beforeTest {
            server = MockWebServer().apply { start() }
            val http = OpenAiHttpClient(
                OpenAiProperties(apiKey = "test-key", baseUrl = server.url("/").toString().trimEnd('/')),
            )
            chat = OpenAiChatClientImpl(http)
        }
        afterTest { server.shutdown() }

        // §3.2 JD 핵심 역량 태그 추출 (jd.extract_competency, temperature=0.1)
        "JD 역량 추출: 문서 프롬프트/스키마로 요청하고 competencyTags 형태로 역직렬화된다" {
            // given — 문서 출력 스키마(§3.2) 형태의 응답
            val content = JsonUtils.toJson(
                """{"competencyTags":[{"tag":"Java","importance":"HIGH"},{"tag":"Spring","importance":"MEDIUM"}]}""",
            )
            server.enqueue(chatResponse(content))
            val request = AiStructuredRequest(
                model = "gpt-4o-mini",
                systemPrompt = JD_COMPETENCY_SYSTEM,
                userPrompt = "백엔드 개발자 채용. 자바/스프링 필수, 쿠버네티스 우대.",
                parameters = AiParameters(temperature = 0.1),
                responseType = CompetencyExtraction::class,
                jsonSchema = """{"competencyTags":[{"tag":"","importance":"HIGH|MEDIUM|LOW"}]}""",
            )

            // when
            val result = chat.generateStructured(request)

            // then — 문서 형태로 역직렬화
            result shouldBe CompetencyExtraction(
                competencyTags = listOf(
                    CompetencyTag(tag = "Java", importance = "HIGH"),
                    CompetencyTag(tag = "Spring", importance = "MEDIUM"),
                ),
            )

            // then — 요청에 문서 프롬프트·스키마가 실림
            val sent = server.takeRequest().body.readUtf8()
            sent shouldContain "채용 공고(JD) 분석 전문가"
            sent shouldContain "중요도 내림차순으로 정렬한다"
            sent shouldContain "\"schema\":{\"competencyTags\":"
        }

        // §3.1 STAR 재구조화 (experience.extract_star, temperature=0.2) — 중첩 스키마
        "STAR 재구조화: 문서의 중첩 스키마(projects→experiences) 형태로 역직렬화된다" {
            // given — 문서 §3.1 출력 스키마 형태
            val content = JsonUtils.toJson(
                """
                {"personalInfo":{"name":"홍길동","phone":"010-1234-5678","email":"hong@test.com"},
                 "education":[{"school":"OO대학교","degree":"학사","period":"2015-2019"}],
                 "certifications":["정보처리기사"],
                 "projects":[{"name":"결제 시스템 개선","period":"2021-2022","role":"백엔드","company":"OO사",
                   "experiences":[{"situation":"트래픽 급증","task":"성능 개선","action":"캐시 도입","result":"응답 40% 단축","competencyTags":["성능최적화"]}]}]}
                """.trimIndent(),
            )
            server.enqueue(chatResponse(content))
            val request = AiStructuredRequest(
                model = "gpt-4o-mini",
                systemPrompt = STAR_EXTRACT_SYSTEM,
                userPrompt = "OO사에서 결제 시스템 트래픽 급증을 캐시 도입으로 해결...",
                parameters = AiParameters(temperature = 0.2),
                responseType = StarExtraction::class,
                jsonSchema = STAR_SCHEMA,
            )

            // when
            val result = chat.generateStructured(request)

            // then — 중첩 구조 끝까지 매핑
            result.personalInfo.name shouldBe "홍길동"
            result.certifications shouldBe listOf("정보처리기사")
            val experience = result.projects.single().experiences.single()
            experience.action shouldBe "캐시 도입"
            experience.competencyTags shouldBe listOf("성능최적화")

            val sent = server.takeRequest().body.readUtf8()
            sent shouldContain "채용 도메인 경력 분석가"
            sent shouldContain "원문에 없는 사실을 절대 지어내지 마라"
        }

        // §3.3 JD 필수/우대 요약 + 일과 추론 (jd.summarize_requirements, temperature=0.2)
        "JD 요약: isAmbiguous/required/preferred/inferredDailyTasks 형태로 역직렬화된다" {
            // given — 문서 §3.3 출력 스키마 형태
            val content = JsonUtils.toJson(
                """
                {"isAmbiguous":false,
                 "required":["Java","Spring Boot"],
                 "preferred":["Kotlin","Kubernetes"],
                 "inferredDailyTasks":["REST API 개발","코드 리뷰"],
                 "coreCompetencies":["백엔드 설계"],
                 "fallbackSummary":""}
                """.trimIndent(),
            )
            server.enqueue(chatResponse(content))
            val request = AiStructuredRequest(
                model = "gpt-4o-mini",
                systemPrompt = JD_SUMMARY_SYSTEM,
                userPrompt = "백엔드 개발자 채용 공고 본문...",
                parameters = AiParameters(temperature = 0.2),
                responseType = JdRequirementSummary::class,
                jsonSchema = JD_SUMMARY_SCHEMA,
            )

            // when
            val result = chat.generateStructured(request)

            // then
            result.isAmbiguous shouldBe false
            result.required shouldBe listOf("Java", "Spring Boot")
            result.inferredDailyTasks shouldBe listOf("REST API 개발", "코드 리뷰")

            val sent = server.takeRequest().body.readUtf8()
            sent shouldContain "isAmbiguous"
        }

        // §3.5 경험 문장 자동 작성 (resume.rewrite_experience, generateText, temperature=0.6)
        "경험 문장 자동 작성: 자유 텍스트 한 문단이 그대로 반환된다" {
            // given — 생성 계열은 평문 텍스트
            val paragraph = "결제 시스템의 트래픽 급증 상황에서 캐시 계층을 도입해 평균 응답 시간을 40% 단축했다."
            server.enqueue(chatResponse(JsonUtils.toJson(paragraph)))
            val request = AiGenerationRequest(
                model = "gpt-4o-mini",
                systemPrompt = RESUME_REWRITE_SYSTEM,
                userPrompt = "원본 STAR + JD 역량 + 톤: 표준",
                parameters = AiParameters(temperature = 0.6, maxTokens = 512),
            )

            // when
            val result = chat.generateText(request)

            // then
            result shouldBe paragraph

            val sent = server.takeRequest().body.readUtf8()
            sent shouldContain "이력서 작성 코치"
            sent shouldNotContain "response_format" // 생성 계열엔 미전송
        }
    }

    /** choices[0].message.content 에 [content]를 담은 chat completion 응답. */
    private fun chatResponse(content: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody("""{"choices":[{"message":{"role":"assistant","content":$content}}]}""")

    // ---- 문서 §3 출력 스키마 (예시 형태 그대로) ----
    private val STAR_SCHEMA = """
        {"personalInfo":{"name":"","phone":"","email":""},
         "education":[{"school":"","degree":"","period":""}],
         "certifications":[""],
         "projects":[{"name":"","period":"","role":"","company":"",
           "experiences":[{"situation":"","task":"","action":"","result":"","competencyTags":[""]}]}]}
    """.trimIndent()

    private val JD_SUMMARY_SCHEMA = """
        {"isAmbiguous":false,"required":[""],"preferred":[""],
         "inferredDailyTasks":[""],"coreCompetencies":[""],"fallbackSummary":""}
    """.trimIndent()

    // ---- 문서 §3 System 프롬프트 ----
    private val JD_COMPETENCY_SYSTEM =
        "당신은 채용 공고(JD) 분석 전문가다. 입력된 JD 본문에서 해당 직무가 요구하는 핵심 역량·기술 스택·도구·자격을 키워드로 추출한다. " +
            "다음 규칙을 지켜라. (1) 의미가 같은 표현은 하나로 합치고 표준화된 명사구로 정규화한다. " +
            "(2) JD에 명시되지 않은 역량을 추측해 추가하지 마라. " +
            "(3) 각 역량의 중요도를 HIGH/MEDIUM/LOW로 판정한다. (4) 중요도 내림차순으로 정렬한다. 출력은 제공된 JSON 스키마를 100% 준수한다."

    private val STAR_EXTRACT_SYSTEM =
        "당신은 채용 도메인 경력 분석가다. 입력된 이력/경력 원문을 분석해 (1) 인적사항·학력·자격/어학 섹션을 분류하고, " +
            "(2) 경력/프로젝트는 각 경험 단위로 STAR로 재구조화하며, (3) 회사·기간·맥락 단서로 경험 카드를 프로젝트 단위로 그룹핑한다. " +
            "원문에 없는 사실을 절대 지어내지 마라. 불확실하면 해당 필드를 빈 문자열로 둔다. 출력은 제공된 JSON 스키마를 100% 준수한다."

    private val JD_SUMMARY_SYSTEM =
        "당신은 채용 공고(JD) 분석 전문가다. 입력된 JD 본문을 분석해 (1) 자격 요건을 필수와 우대로 분리한다. " +
            "(2) 담당 업무를 근거로 하루 일과를 현실적으로 추론한다. (3) 핵심 역량을 도출한다. " +
            "필수/우대 구분이 명확하지 않으면 isAmbiguous를 true로 두고 fallbackSummary에 요약한다. 출력은 제공된 JSON 스키마를 100% 준수한다."

    private val RESUME_REWRITE_SYSTEM =
        "당신은 IT/직무 이력서 작성 코치다. 입력으로 받은 원본 STAR와 대상 JD의 핵심 역량을 바탕으로 해당 경험을 이력서에 들어갈 한 문단으로 재작성한다. " +
            "STAR에 담긴 사실은 절대 바꾸거나 지어내지 마라. 출력은 부가 설명 없이 재작성된 문단 텍스트만 반환한다."

    // ---- 문서 §3 출력 스키마에 대응하는 도메인 타입 ----
    data class CompetencyExtraction(val competencyTags: List<CompetencyTag>)
    data class CompetencyTag(val tag: String, val importance: String)

    data class StarExtraction(
        val personalInfo: PersonalInfo,
        val education: List<Education>,
        val certifications: List<String>,
        val projects: List<Project>,
    )
    data class PersonalInfo(val name: String, val phone: String, val email: String)
    data class Education(val school: String, val degree: String, val period: String)
    data class Project(
        val name: String,
        val period: String,
        val role: String,
        val company: String,
        val experiences: List<Experience>,
    )
    data class Experience(
        val situation: String,
        val task: String,
        val action: String,
        val result: String,
        val competencyTags: List<String>,
    )

    data class JdRequirementSummary(
        val isAmbiguous: Boolean,
        val required: List<String>,
        val preferred: List<String>,
        val inferredDailyTasks: List<String>,
        val coreCompetencies: List<String>,
        val fallbackSummary: String,
    )
}
