package com.jobdori.api.support.logging

import com.jobdori.common.logger.LoggerExtension.log
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 키 이름은 Datadog 예약 속성이다. 임의로 바꾸면 Log Explorer의 기본 facet과 UI 특별 처리가 깨진다.
 */
object MdcKeys {
    const val HTTP_METHOD = "http.method"
    const val HTTP_URL = "http.url"
    const val HTTP_STATUS_CODE = "http.status_code"
    const val CLIENT_IP = "network.client.ip"
    const val USER_ID = "usr.id"
    const val GRAPHQL_OPERATION = "graphql.operation"

    val ALL = listOf(HTTP_METHOD, HTTP_URL, HTTP_STATUS_CODE, CLIENT_IP, USER_ID, GRAPHQL_OPERATION)
}

@Component
class RequestLoggingFilter : OncePerRequestFilter(), Ordered {

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 10

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val startedNanos = System.nanoTime()
        MDC.put(MdcKeys.HTTP_METHOD, request.method)
        MDC.put(MdcKeys.HTTP_URL, request.requestURI)
        clientIp(request)?.let { MDC.put(MdcKeys.CLIENT_IP, it) }

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.put(MdcKeys.HTTP_STATUS_CODE, response.status.toString())
            logAccess(request, response, System.nanoTime() - startedNanos)
            // 톰캣 스레드는 재사용된다. 지우지 않으면 다음 요청 로그에 이전 사용자 ID가 섞인다.
            // dd-trace가 넣는 키까지 날리지 않도록 우리가 넣은 키만 제거한다.
            MdcKeys.ALL.forEach(MDC::remove)
        }
    }

    private fun logAccess(request: HttpServletRequest, response: HttpServletResponse, durationNanos: Long) {
        if (request.requestURI.startsWith(ACTUATOR_PREFIX)) return
        log.atInfo {
            message = "http_request ${request.method} ${request.requestURI} ${response.status}"
            // Datadog 표준 duration은 나노초 단위다
            payload = mapOf("duration" to durationNanos)
        }
    }

    // ALB 뒤라 remoteAddr은 항상 로드밸런서다. XFF의 첫 값이 실제 클라이언트.
    private fun clientIp(request: HttpServletRequest): String? =
        request.getHeader("X-Forwarded-For")
            ?.substringBefore(',')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: request.remoteAddr

    private companion object {
        private const val ACTUATOR_PREFIX = "/actuator"
    }
}
