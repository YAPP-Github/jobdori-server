package com.jobdori.api.application.profile.dto.request

import com.jobdori.core.application.profile.PolishStructure
import com.jobdori.core.domain.profile.ProfilePolicy
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PolishExperienceRequest(
    @field:NotBlank
    @field:Size(max = ProfilePolicy.MAX_TITLE_LENGTH)
    val title: String = "",

    @field:NotBlank
    @field:Size(max = ProfilePolicy.MAX_CONTENTS_LENGTH)
    val description: String = "",

    val structure: PolishStructure? = null,

    @field:Size(max = 200)
    val instruction: String? = null,

    val jdId: String? = null,
)
