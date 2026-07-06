package com.jobdori.infrastructure.client.jd

import com.jobdori.core.domain.jd.error.JdCrawlErrorCode
import com.jobdori.core.domain.jd.error.JdCrawlException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class JdUrlGuardTest : StringSpec({

    val guard = JdUrlGuard()

    fun blocked(url: String) =
        shouldThrow<JdCrawlException> { guard.validate(url) }.errorCode shouldBe JdCrawlErrorCode.E400_JD_INVALID_URL

    "http/https가 아닌 스킴은 차단한다" {
        blocked("file:///etc/passwd")
        blocked("ftp://example.com/x")
        blocked("gopher://example.com/")
    }

    "루프백 주소는 차단한다" {
        blocked("http://127.0.0.1/x")
        blocked("http://127.0.0.1:8080/actuator")
    }

    "링크로컬(클라우드 메타데이터) 주소는 차단한다" {
        blocked("http://169.254.169.254/latest/meta-data/iam/security-credentials/")
    }

    "사설 대역(10/172.16/192.168) 주소는 차단한다" {
        blocked("http://10.0.0.5/internal")
        blocked("http://172.16.0.1/")
        blocked("http://192.168.1.1/admin")
    }

    "와일드카드(0.0.0.0) 주소는 차단한다" {
        blocked("http://0.0.0.0/")
    }

    "호스트가 없는 URL은 차단한다" {
        blocked("http:///path-only")
    }

    "공개 IP는 통과시킨다" {
        shouldNotThrowAny { guard.validate("http://8.8.8.8/robots.txt") }
        shouldNotThrowAny { guard.validate("https://8.8.8.8/") }
    }

})
