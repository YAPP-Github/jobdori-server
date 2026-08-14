package com.jobdori.api.application.jd.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.domain.jd.JdPolicy
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class JdRegisterRequest(
    @field:URL(message = "올바른 공고 출처 URL 형식으로 입력해 주세요.")
    val sourceUrl: String? = null,

    @field:Size(
        min = JdPolicy.MIN_JD_BODY_LENGTH,
        max = JdPolicy.MAX_JD_LENGTH,
        message = "채용 공고 본문은 {min}자 이상 {max}자 이하로 입력해 주세요.",
    )
    val body: String? = null,
) {

    // body가 있으면 크롤 없이 body로 처리하고 sourceUrl은 출처 메타로만 저장한다(다중 공고 후보 재등록 시 출처 보존)
    @JsonIgnore
    @AssertTrue(message = "공고 출처 URL 또는 채용 공고 본문을 입력해 주세요.")
    fun isAtLeastOne(): Boolean = !sourceUrl.isNullOrBlank() || !body.isNullOrBlank()

}
