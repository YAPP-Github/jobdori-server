package com.jobdori.api.application.jd.dto.request

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import jakarta.validation.Validator

class JdRegisterRequestTest : StringSpec({

    val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    "sourceUrl만 채우면 유효하다" {
        // given
        val request = JdRegisterRequest(sourceUrl = "https://example.com/jd", body = null)

        // when & then
        validator.validate(request).shouldBeEmpty()
    }

    "body만 채우면 유효하다(최소 길이 이상)" {
        // given
        val request = JdRegisterRequest(sourceUrl = null, body = "여러 공고가 섞인 본문 ".repeat(50))

        // when & then
        validator.validate(request).shouldBeEmpty()
    }

    "sourceUrl과 body를 동시에 채우면 위반된다" {
        // given
        val request = JdRegisterRequest(sourceUrl = "https://example.com/jd", body = "본문 ".repeat(200))

        // when
        val violations = validator.validate(request)

        // then
        violations.size shouldBe 1
        violations.single().message shouldBe "sourceUrl 또는 body 중 정확히 하나여야 합니다"
    }

    "sourceUrl과 body가 모두 비어 있으면 위반된다" {
        // given
        val request = JdRegisterRequest(sourceUrl = null, body = null)

        // when
        val violations = validator.validate(request)

        // then
        violations.size shouldBe 1
        violations.single().message shouldBe "sourceUrl 또는 body 중 정확히 하나여야 합니다"
    }

    "body가 최소 길이보다 짧으면 위반된다" {
        // given
        val request = JdRegisterRequest(sourceUrl = null, body = "너무 짧은 본문")

        // when
        val violations = validator.validate(request)

        // then
        violations.size shouldBe 1
        violations.single().propertyPath.toString() shouldBe "body"
    }

    "sourceUrl이 URL 형식이 아니면 위반된다" {
        // given
        val request = JdRegisterRequest(sourceUrl = "not-a-valid-url", body = null)

        // when
        val violations = validator.validate(request)

        // then
        violations.size shouldBe 1
        violations.single().propertyPath.toString() shouldBe "sourceUrl"
    }

    "isExactlyOne은 정확히 하나만 채워졌을 때만 true를 반환한다" {
        JdRegisterRequest(sourceUrl = "https://example.com", body = null).isExactlyOne() shouldBe true
        JdRegisterRequest(sourceUrl = null, body = "본문").isExactlyOne() shouldBe true
        JdRegisterRequest(sourceUrl = "https://example.com", body = "본문").isExactlyOne() shouldBe false
        JdRegisterRequest(sourceUrl = null, body = null).isExactlyOne() shouldBe false
        JdRegisterRequest(sourceUrl = "  ", body = "본문").isExactlyOne() shouldBe true
    }

})