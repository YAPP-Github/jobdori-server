package com.jobdori.api.application.consent.dto.request

import com.jobdori.core.domain.consent.ConsentSource
import jakarta.validation.constraints.AssertTrue

data class UpdateOptionalConsentsRequest(
    val research: Boolean? = null,
    val marketingEmail: Boolean? = null,
    val source: ConsentSource,
) {
    @AssertTrue(message = "변경할 선택 동의를 입력해 주세요.")
    fun hasOptionalConsentChange(): Boolean = research != null || marketingEmail != null
}
