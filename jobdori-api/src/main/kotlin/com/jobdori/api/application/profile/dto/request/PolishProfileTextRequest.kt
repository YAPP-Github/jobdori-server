package com.jobdori.api.application.profile.dto.request

import com.jobdori.core.application.profile.ProfilePolishKind
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PolishProfileTextRequest(
    @field:NotBlank
    @field:Size(max = 600)
    val text: String = "",

    val kind: ProfilePolishKind = ProfilePolishKind.CORE_COMPETENCY,
)
