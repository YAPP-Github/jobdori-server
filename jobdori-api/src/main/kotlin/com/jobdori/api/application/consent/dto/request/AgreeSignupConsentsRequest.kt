package com.jobdori.api.application.consent.dto.request

import jakarta.validation.constraints.AssertTrue

data class AgreeSignupConsentsRequest(
    val terms: Boolean,
    val privacy: Boolean,
    val research: Boolean? = null,
    val marketingEmail: Boolean? = null,
) {
    @AssertTrue(message = "필수 약관에 동의해 주세요.")
    fun isRequiredConsentsAgreed(): Boolean = terms && privacy
}
