package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.common.json.JsonUtils
import com.jobdori.core.application.ai.command.AiGenerationRequest
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

/**
 * [OpenAiChatClientImpl] + [OpenAiHttpClient] MockWebServer 통합테스트.
 * 문서 §5: "AiClientImpl 구조화 출력·역직렬화는 MockWebServer 통합테스트".
 */
class OpenAiChatClientImplTest : StringSpec() {

    private lateinit var server: MockWebServer
    private lateinit var client: OpenAiChatClientImpl

    init {
        beforeTest {
            server = MockWebServer().apply { start() }
            val http = OpenAiHttpClient(
                OpenAiProperties(apiKey = "test-key", baseUrl = server.url("/").toString().trimEnd('/')),
            )
            client = OpenAiChatClientImpl(http)
        }
        afterTest { server.shutdown() }

        "generateText는 /chat/completions를 호출하고 첫 choice의 content를 반환한다" {
            // given
            server.enqueue(jsonResponse("""{"choices":[{"message":{"role":"assistant","content":"재작성된 문단"}}]}"""))
            val request = AiGenerationRequest(
                model = "gpt-4o-mini",
                systemPrompt = "너는 이력서 코치다",
                userPrompt = "이 경험을 다듬어줘",
                parameters = AiParameters(temperature = 0.6),
            )

            // when
            val result = client.generateText(request)

            // then
            result shouldBe "재작성된 문단"

            val recorded = server.takeRequest()
            recorded.method shouldBe "POST"
            recorded.path shouldBe "/chat/completions"
            recorded.getHeader("Authorization") shouldBe "Bearer test-key"
            val sent = recorded.body.readUtf8()
            sent shouldContain "\"model\":\"gpt-4o-mini\""
            sent shouldContain "\"role\":\"system\""
            sent shouldContain "\"role\":\"user\""
            sent shouldContain "\"temperature\":0.6"
            sent shouldNotContain "response_format"   // 텍스트 경로엔 포맷 미전송
        }

        "generateText는 choices가 비면 빈 문자열을 반환한다" {
            // given
            server.enqueue(jsonResponse("""{"choices":[]}"""))
            val request = textRequest()

            // when / then
            client.generateText(request) shouldBe ""
        }

        "generateStructured는 response_format(json_schema)를 보내고 content JSON을 타입으로 역직렬화한다" {
            // given — content는 JSON 문자열(escape)로 들어온다
            val content = JsonUtils.toJson("""{"tag":"Java","importance":"HIGH"}""")
            server.enqueue(jsonResponse("""{"choices":[{"message":{"role":"assistant","content":$content}}]}"""))
            val request = AiStructuredRequest(
                model = "gpt-4o-mini",
                systemPrompt = "JD 분석가",
                userPrompt = "이 JD를 분석해",
                parameters = AiParameters(temperature = 0.1),
                responseType = CompetencyTag::class,
                jsonSchema = """{"type":"object","properties":{"tag":{"type":"string"}}}""",
            )

            // when
            val result = client.generateStructured(request)

            // then — 역직렬화 결과
            result shouldBe CompetencyTag(tag = "Java", importance = "HIGH")

            // then — 요청 본문: 스키마가 중첩 객체로(문자열 escape가 아니라) 박히고 strict/name 포함
            val sent = server.takeRequest().body.readUtf8()
            sent shouldContain "\"response_format\""
            sent shouldContain "\"type\":\"json_schema\""
            sent shouldContain "\"name\":\"CompetencyTag\""
            sent shouldContain "\"strict\":true"
            sent shouldContain "\"schema\":{\"type\":\"object\""   // 중첩 객체로 재직렬화됨
        }

        "429 응답은 E429_AI_RATE_LIMITED로 매핑된다" {
            server.enqueue(MockResponse().setResponseCode(429))
            shouldThrow<AiException> { client.generateText(textRequest()) }
                .errorCode shouldBe AiErrorCode.E429_AI_RATE_LIMITED
        }

        "5xx 응답은 E503_AI_UNAVAILABLE로 매핑된다" {
            server.enqueue(MockResponse().setResponseCode(500))
            shouldThrow<AiException> { client.generateText(textRequest()) }
                .errorCode shouldBe AiErrorCode.E503_AI_UNAVAILABLE
        }

        "429 외 4xx(예: 401)는 E500_AI_GENERATION_FAILED로 매핑된다" {
            server.enqueue(MockResponse().setResponseCode(401))
            shouldThrow<AiException> { client.generateText(textRequest()) }
                .errorCode shouldBe AiErrorCode.E500_AI_GENERATION_FAILED
        }

        "본문이 빈 200 응답은 E500_AI_GENERATION_FAILED로 매핑된다" {
            server.enqueue(MockResponse().setResponseCode(200))
            shouldThrow<AiException> { client.generateText(textRequest()) }
                .errorCode shouldBe AiErrorCode.E500_AI_GENERATION_FAILED
        }
    }

    private fun jsonResponse(body: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private fun textRequest() = AiGenerationRequest(
        model = "gpt-4o-mini",
        systemPrompt = "s",
        userPrompt = "u",
        parameters = AiParameters(),
    )

    data class CompetencyTag(val tag: String, val importance: String)
}
