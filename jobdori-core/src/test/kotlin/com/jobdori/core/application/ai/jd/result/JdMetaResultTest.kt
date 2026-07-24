package com.jobdori.core.application.ai.jd.result

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class JdMetaResultTest : StringSpec({

    "채용 공고 여부가 누락되면 채용 공고가 아닌 것으로 처리한다" {
        JdMetaResult().isJobPosting shouldBe false
    }

    "JD 실체 판별 시 빈 문자열과 공백만 있는 목록을 무시한다" {
        JdMetaResult(
            positionTitle = " ",
            responsibilities = listOf(""),
            requiredExperiences = listOf("  "),
            preferredExperiences = listOf("\t"),
        ).hasNoJdSubstance() shouldBe true
    }

    "관련 목록에 비어 있지 않은 항목이 있으면 JD 실체가 있는 것으로 처리한다" {
        JdMetaResult(
            responsibilities = listOf(" ", "API 개발"),
        ).hasNoJdSubstance() shouldBe false
    }

})
