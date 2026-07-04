package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.time.Duration

/**
 * OpenAI HTTP 호출 공통 컴포넌트.
 * RestClient 설정·인증 헤더·에러 매핑(429/5xx/timeout → [AiException])을 한곳에 모아
 * 채팅([OpenAiChatClientImpl])과 임베딩([OpenAiEmbeddingClientImpl]) 구현이 공유한다.
 */
@Component
class OpenAiHttpClient(
    private val properties: OpenAiProperties,
) {
    private val restClient = RestClient.builder()
        .baseUrl(properties.baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${properties.apiKey}")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate")
        // JDK HttpClient 기반. SimpleClientHttpRequestFactory(HttpURLConnection)는 401 등 에러 본문을
        // 삼켜([no body]) OpenAI 에러 JSON이 유실되므로 사용하지 않는다.
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
            ).apply { setReadTimeout(Duration.ofSeconds(60)) },  // 추출 응답 김 대비
        )
        .build()

    fun <T : Any> post(uri: String, body: Any, responseType: Class<T>): T =
        try {
            restClient.post().uri(uri)
                .body(body).retrieve().body(responseType)
                ?: throw AiException("빈 응답", AiErrorCode.E500_AI_GENERATION_FAILED)
        } catch (e: HttpClientErrorException.TooManyRequests) {
            log.warn(e) { "OpenAI 요청 한도 초과: uri=$uri" }
            throw AiException("AI 요청 한도 초과", AiErrorCode.E429_AI_RATE_LIMITED, e)
        } catch (e: HttpClientErrorException) {   // 429 외 4xx(401 키 오류, 400 잘못된 요청 등)
            log.warn(e) { "OpenAI 요청 오류: uri=$uri, status=${e.statusCode}" }
            throw AiException("AI 요청 오류(${e.statusCode})", AiErrorCode.E500_AI_GENERATION_FAILED, e)
        } catch (e: HttpServerErrorException) {
            log.warn(e) { "OpenAI 서버 오류: uri=$uri, status=${e.statusCode}" }
            throw AiException("AI 서비스 오류(${e.statusCode})", AiErrorCode.E503_AI_UNAVAILABLE, e)
        } catch (e: ResourceAccessException) {
            log.warn(e) { "OpenAI 응답 타임아웃: uri=$uri" }
            throw AiException("AI 응답 타임아웃", AiErrorCode.E504_AI_TIMEOUT, e)
        }
}
