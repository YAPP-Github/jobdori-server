package com.jobdori.infrastructure.client.jd

import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.jd.client.JdCrawlerClient
import com.jobdori.core.domain.jd.error.JdCrawlErrorCode
import com.jobdori.core.domain.jd.error.JdCrawlException
import org.jsoup.Jsoup
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Duration

/**
 * 정적(Jsoup) JD 크롤러 = [JdCrawlerClient] 구현.
 */
@Component
class StaticJdCrawlerClient(
    private val properties: JdCrawlerProperties,
    private val urlGuard: JdUrlGuard,
    private val nextDataJdParser: NextDataJdParser,
    private val jsonLdJdParser: JsonLdJdParser,
) : JdCrawlerClient {

    private val noiseSelector =
        "style, noscript, nav, footer, aside, iframe, svg, form, button, input, select"

    private val restClient = RestClient.builder()
        .requestFactory(
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(Duration.ofSeconds(5))
                setReadTimeout(Duration.ofSeconds(10))
            },
        )
        .build()

    override fun fetchBody(url: String): String {
        urlGuard.validate(url)                              // SSRF: 스킴·내부주소 차단(요청 전)
        val html = fetchHtml(url)
        // 구조화 데이터 우선: __NEXT_DATA__(Next.js) → JSON-LD JobPosting → 일반 본문 추출 폴백
        val body = nextDataJdParser.parse(url, html)
            ?: jsonLdJdParser.parse(url, html)
            ?: extractText(html, url)
        if (body.length >= properties.minBodyLength) return body

        log.warn { "JD 정적 크롤링 본문 부족: url=$url, length=${body.length}" }
        throw JdCrawlException("본문 수집 실패(붙여넣기로 입력): $url", JdCrawlErrorCode.E422_JD_FETCH_FAILED)
    }

    private fun fetchHtml(url: String): ByteArray? =
        try {
            restClient.get().uri(url)
                .header("User-Agent", properties.userAgent)
                .exchange { _, response ->
                    val status = response.statusCode
                    when {
                        status.is4xxClientError ->
                            throw JdCrawlException("접근 불가: $url", JdCrawlErrorCode.E422_JD_ACCESS_DENIED)
                        !status.is2xxSuccessful -> null
                        else -> response.body.use { readLimited(it, properties.maxBodyBytes) }
                    }
                }
        } catch (e: JdCrawlException) {
            throw e
        } catch (e: Exception) {
            log.warn(e) { "JD 정적 크롤링 실패: url=$url" }   // 네트워크·5xx 등 → 약함으로 처리
            null
        }

    private fun readLimited(input: InputStream, maxBytes: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        var total = 0
        while (true) {
            val read = input.read(chunk)
            if (read < 0) break
            val remaining = maxBytes - total
            if (read >= remaining) {
                out.write(chunk, 0, remaining)
                break
            }
            out.write(chunk, 0, read)
            total += read
        }
        return out.toByteArray()
    }

    private fun extractText(html: ByteArray?, url: String): String {
        if (html == null || html.isEmpty()) return ""
        val document = Jsoup.parse(ByteArrayInputStream(html), null, url)
        val body = document.apply { select(noiseSelector).remove() }.body() ?: return ""
        return body.text().trim()
    }

}
