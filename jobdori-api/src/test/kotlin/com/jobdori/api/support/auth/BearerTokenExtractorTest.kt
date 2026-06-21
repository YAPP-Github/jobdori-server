package com.jobdori.api.support.auth

import com.jobdori.core.domain.auth.error.InvalidAuthTokenException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class BearerTokenExtractorTest : StringSpec({

    "Bearer Authorization 헤더에서 토큰을 추출한다" {
        // when & then
        BearerTokenExtractor.extract("Bearer access-token") shouldBe "access-token"
    }

    "Authorization 헤더가 없거나 형식이 잘못되면 인증 실패로 처리한다" {
        // when & then
        listOf(null, "", "Basic token", "Bearer", "bearer access-token").forEach { authorization ->
            shouldThrow<InvalidAuthTokenException> {
                BearerTokenExtractor.extract(authorization)
            }
        }
    }

})
