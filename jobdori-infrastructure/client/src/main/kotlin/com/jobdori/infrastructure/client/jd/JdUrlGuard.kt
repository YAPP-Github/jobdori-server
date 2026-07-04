package com.jobdori.infrastructure.client.jd

import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.domain.jd.error.JdCrawlErrorCode
import com.jobdori.core.domain.jd.error.JdCrawlException
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.URI

/**
 * SSRF 방어 — 크롤 대상 URL이 외부 공개 HTTP(S) 자원인지 검증.
 */
@Component
class JdUrlGuard {

    fun validate(url: String) {
        val uri = runCatching { URI(url) }.getOrElse { throw invalid("URL 파싱 실패: $url") }

        val scheme = uri.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") throw invalid("허용되지 않은 스킴: $scheme")

        val host = uri.host ?: throw invalid("호스트 없음: $url")

        val addresses = runCatching { InetAddress.getAllByName(host) }
            .getOrElse { throw invalid("호스트 해석 실패: $host") }

        addresses.firstOrNull { it.isBlocked() }?.let {
            throw invalid("내부 네트워크 주소 차단: $host -> ${it.hostAddress}")
        }
    }

    private fun InetAddress.isBlocked(): Boolean =
        isLoopbackAddress || isAnyLocalAddress || isLinkLocalAddress || isSiteLocalAddress || isMulticastAddress

    private fun invalid(reason: String): JdCrawlException {
        log.warn { "JD 크롤 URL 차단: $reason" }
        return JdCrawlException(reason, JdCrawlErrorCode.E400_JD_INVALID_URL)
    }

}
