package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

/**
 * [OpenAiEmbeddingClientImpl] + [OpenAiHttpClient] MockWebServer 통합테스트.
 */
class OpenAiEmbeddingClientImplTest : StringSpec() {

    private lateinit var server: MockWebServer
    private lateinit var client: OpenAiEmbeddingClientImpl

    init {
        beforeTest {
            server = MockWebServer().apply { start() }
            val http = OpenAiHttpClient(
                OpenAiProperties(apiKey = "test-key", baseUrl = server.url("/").toString().trimEnd('/')),
            )
            client = OpenAiEmbeddingClientImpl(http)
        }
        afterTest { server.shutdown() }

        "embed는 /embeddings를 단일 모델로 호출하고 첫 임베딩 벡터를 반환한다" {
            // given
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"data":[{"embedding":[0.1,0.2,0.3]}]}"""),
            )

            // when
            val result = client.embed("매칭 대상 텍스트")

            // then
            result.toList() shouldBe listOf(0.1f, 0.2f, 0.3f)

            val recorded = server.takeRequest()
            recorded.method shouldBe "POST"
            recorded.path shouldBe "/embeddings"
            val sent = recorded.body.readUtf8()
            sent shouldContain "\"model\":\"text-embedding-3-small\""
            sent shouldContain "\"input\":\"매칭 대상 텍스트\""
        }

        "embed는 data가 비면 E500_AI_GENERATION_FAILED를 던진다" {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody("""{"data":[]}"""),
            )

            shouldThrow<AiException> { client.embed("text") }
                .errorCode shouldBe AiErrorCode.E500_AI_GENERATION_FAILED
        }
    }
}
