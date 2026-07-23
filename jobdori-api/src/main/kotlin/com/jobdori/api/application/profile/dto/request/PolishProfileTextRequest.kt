package com.jobdori.api.application.profile.dto.request

import com.jobdori.core.application.profile.PolishStructure
import com.jobdori.core.application.profile.ProfilePolishKind
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PolishProfileTextRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val text: String = "",

    val kind: ProfilePolishKind = ProfilePolishKind.CORE_COMPETENCY,

    val structure: PolishStructure? = null,

    @field:Size(max = 200)
    val instruction: String? = null,

    @field:Size(max = 100)
    val title: String? = null,

    val jdId: String? = null,
)
