package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateExperienceProjectRequest(
    @field:Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @field:Size(max = 100)
    val name: String? = null,

    @field:Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @field:Size(max = 500)
    val summary: String? = null,

    @field:Valid
    val period: PeriodRequest? = null,

    @field:Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @field:Size(max = 100)
    val role: String? = null,
)
