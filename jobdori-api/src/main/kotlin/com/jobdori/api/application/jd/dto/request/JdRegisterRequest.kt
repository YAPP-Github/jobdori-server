package com.jobdori.api.application.jd.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.domain.jd.JdPolicy
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class JdRegisterRequest(
    @field:URL
    val sourceUrl: String? = null,

    @field:Size(min = JdPolicy.MIN_JD_BODY_LENGTH, max = JdPolicy.MAX_JD_LENGTH)
    val body: String? = null,
) {

    @JsonIgnore
    @AssertTrue(message = "sourceUrl 또는 body 중 정확히 하나여야 합니다")
    fun isExactlyOne(): Boolean = sourceUrl.isNullOrBlank() != body.isNullOrBlank()

}
