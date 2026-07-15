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
import java.net.HttpURLConnection
import java.net.URI
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

    // 워드프레스 등 본문 컨테이너 우선순위(사이트 메뉴·사이드바가 nav 밖에 있는 경우 노이즈 제거)
    private val contentSelectors = listOf(".entry-content", "article", "main")

    // 자동 리다이렉트 비활성화: 켜두면 최초 URL만 guard를 통과하고 302로 내부주소(메타데이터 등)로 우회 가능 → 홉마다 직접 검증한다
    private val restClient = RestClient.builder()
        .requestFactory(
            object : SimpleClientHttpRequestFactory() {
                override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
                    super.prepareConnection(connection, httpMethod)
                    connection.instanceFollowRedirects = false
                }
            }.apply {
                setConnectTimeout(Duration.ofSeconds(5))
                setReadTimeout(Duration.ofSeconds(10))
            },
        )
        .build()

    // 호출당 사용량 로그(jd_crawl ...) 한 줄을 남긴다. ai_call 로그와 같은 key=value 형식으로,
    // 키 이름은 로그 쿼리 호환성을 위해 함부로 바꾸지 않는다.
    override fun fetchBody(url: String): String {
        val started = System.nanoTime()
        return runCatching {
            val html = fetchHtml(url)
            // 구조화 데이터 우선: __NEXT_DATA__(Next.js) → JSON-LD JobPosting → 일반 본문 추출 폴백
            val body = nextDataJdParser.parse(url, html)
                ?: jsonLdJdParser.parse(url, html)
                ?: extractText(html, url)
            if (body.length < properties.minBodyLength) {
                log.warn { "JD 정적 크롤링 본문 부족: url=$url, length=${body.length}" }
                throw JdCrawlException("본문 수집 실패(붙여넣기로 입력): $url", JdCrawlErrorCode.E422_JD_FETCH_FAILED)
            }
            body
        }
            .onSuccess { body ->
                log.info { "jd_crawl success=true latencyMs=${elapsedMs(started)} bodyLength=${body.length} url=$url" }
            }
            .onFailure { e ->
                log.warn { "jd_crawl success=false latencyMs=${elapsedMs(started)} error=${e.javaClass.simpleName} url=$url" }
            }
            .getOrThrow()
    }

    private fun elapsedMs(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000

    private fun fetchHtml(startUrl: String): ByteArray? {
        var url = startUrl
        repeat(MAX_REDIRECTS + 1) {
            urlGuard.validate(url)                          // SSRF: 스킴·내부주소 차단(홉마다 재검증)
            val step = try {
                restClient.get().uri(url)
                    .header("User-Agent", properties.userAgent)
                    .exchange { _, response ->
                        val status = response.statusCode
                        when {
                            status.is3xxRedirection -> Redirect(
                                response.headers.location?.let { URI(url).resolve(it).toString() }
                                    ?: throw JdCrawlException("리다이렉트 위치 없음: $url", JdCrawlErrorCode.E422_JD_FETCH_FAILED),
                            )
                            status.is4xxClientError ->
                                throw JdCrawlException("접근 불가: $url", JdCrawlErrorCode.E422_JD_ACCESS_DENIED)
                            !status.is2xxSuccessful -> Fetched(null)
                            else -> Fetched(response.body.use { readLimited(it, properties.maxBodyBytes) })
                        }
                    }
            } catch (e: JdCrawlException) {
                throw e
            } catch (e: Exception) {
                log.warn(e) { "JD 정적 크롤링 실패: url=$url" }   // 네트워크·5xx 등 → 약함으로 처리
                return null
            }
            when (step) {
                is Fetched -> return step.bytes
                is Redirect -> url = step.location
            }
        }
        log.warn { "JD 리다이렉트 횟수 초과: $startUrl" }
        return null
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
        document.select(noiseSelector).remove()
        val body = document.body() ?: return ""
        // 본문 컨테이너가 충분한 길이면 사이트 크롬(메뉴·사이드바) 제외하고 그 영역만, 아니면 전체 body
        val main = contentSelectors.asSequence()
            .mapNotNull { document.selectFirst(it)?.text()?.trim() }
            .firstOrNull { it.length >= properties.minBodyLength }
        return main ?: body.text().trim()
    }

    private sealed interface FetchStep
    private data class Redirect(val location: String) : FetchStep
    private data class Fetched(val bytes: ByteArray?) : FetchStep

    companion object {
        private const val MAX_REDIRECTS = 5
    }

}
