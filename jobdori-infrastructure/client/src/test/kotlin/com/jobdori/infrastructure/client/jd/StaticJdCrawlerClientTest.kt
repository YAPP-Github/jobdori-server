package com.jobdori.infrastructure.client.jd

import com.jobdori.core.domain.jd.error.JdCrawlErrorCode
import com.jobdori.core.domain.jd.error.JdCrawlException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.mockk
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class StaticJdCrawlerClientTest : StringSpec() {

    private lateinit var server: MockWebServer

    // MockWebServer는 loopback(127.0.0.1)이라 실제 JdUrlGuard면 차단된다 → 크롤 로직만 보려고 통과 스텁 주입
    private val urlGuard = mockk<JdUrlGuard>(relaxed = true)

    private fun client(properties: JdCrawlerProperties) = StaticJdCrawlerClient(properties, urlGuard, NextDataJdParser(), JsonLdJdParser())

    init {
        beforeTest { server = MockWebServer().apply { start() } }
        afterTest { server.shutdown() }

        "본문을 추출하되 스크립트·네비·푸터 등 비본문 텍스트는 제외한다" {
            server.enqueue(
                MockResponse().setBody(
                    "<html><body>" +
                        "<nav>로그인 회원가입</nav>" +
                        "<script>var x='노이즈'</script>" +
                        "<article>담당 업무: 백엔드 서버 개발</article>" +
                        "<footer>회사소개 이용약관</footer>" +
                        "</body></html>",
                ),
            )

            val body = client(JdCrawlerProperties(minBodyLength = 5)).fetchBody(server.url("/jd").toString())

            body shouldContain "담당 업무"
            body shouldNotContain "회원가입"
            body shouldNotContain "이용약관"
            body shouldNotContain "노이즈"
        }

        "요청 전에 URL 가드로 SSRF 검증을 먼저 수행한다" {
            server.enqueue(MockResponse().setBody("<html><body><article>담당 업무 상세</article></body></html>"))
            val url = server.url("/jd").toString()

            client(JdCrawlerProperties(minBodyLength = 5)).fetchBody(url)

            verify(exactly = 1) { urlGuard.validate(url) }
        }

        "리다이렉트를 자동으로 따르지 않고 홉마다 URL 가드로 재검증한다(SSRF 우회 차단)" {
            val redirectTarget = server.url("/final").toString()
            server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", redirectTarget))
            server.enqueue(MockResponse().setBody("<html><body><article>담당 업무 상세 내용</article></body></html>"))
            val start = server.url("/jd").toString()

            val body = client(JdCrawlerProperties(minBodyLength = 5)).fetchBody(start)

            body shouldContain "담당 업무"
            verify(exactly = 1) { urlGuard.validate(start) }
            verify(exactly = 1) { urlGuard.validate(redirectTarget) }   // 리다이렉트 목적지도 검증됨
        }

        "User-Agent 헤더를 실어 요청한다" {
            server.enqueue(MockResponse().setBody("<html><body><article>담당 업무 상세</article></body></html>"))

            client(JdCrawlerProperties(minBodyLength = 5)).fetchBody(server.url("/jd").toString())

            server.takeRequest().getHeader("User-Agent") shouldContain "JobdoriBot"
        }

        "접근 거부(403)면 JdCrawlException(E422_JD_ACCESS_DENIED)" {
            server.enqueue(MockResponse().setResponseCode(403))

            shouldThrow<JdCrawlException> {
                client(JdCrawlerProperties(minBodyLength = 5)).fetchBody(server.url("/x").toString())
            }.errorCode shouldBe JdCrawlErrorCode.E422_JD_ACCESS_DENIED
        }

        "추출 본문이 임계치 미만이면 JdCrawlException(E422_JD_FETCH_FAILED)로 붙여넣기를 유도한다" {
            server.enqueue(MockResponse().setBody("<html><body><p>너무 짧은 공고</p></body></html>"))

            shouldThrow<JdCrawlException> {
                client(JdCrawlerProperties(minBodyLength = 500)).fetchBody(server.url("/short").toString())
            }.errorCode shouldBe JdCrawlErrorCode.E422_JD_FETCH_FAILED
        }

        "네트워크/서버 오류(500)도 수집 실패(E422_JD_FETCH_FAILED)로 귀결한다" {
            server.enqueue(MockResponse().setResponseCode(500))

            shouldThrow<JdCrawlException> {
                client(JdCrawlerProperties(minBodyLength = 5)).fetchBody(server.url("/err").toString())
            }.errorCode shouldBe JdCrawlErrorCode.E422_JD_FETCH_FAILED
        }

        "응답 본문을 maxBodyBytes 상한까지만 읽는다(초과분은 버림)" {
            val head = "머리내용 ".repeat(40)   // 상한 안에 들어오는 앞부분
            server.enqueue(
                MockResponse().setBody(
                    "<html><body><article>$head<span>꼬리마커초과분</span></article></body></html>",
                ),
            )

            val body = client(JdCrawlerProperties(minBodyLength = 5, maxBodyBytes = 200))
                .fetchBody(server.url("/big").toString())

            body shouldContain "머리내용"
            body shouldNotContain "꼬리마커초과분"   // 200바이트 이후는 읽지 않아 추출에서 제외
        }
    }

}
