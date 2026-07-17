package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateExperienceProjectRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:NotBlank
    @field:Size(max = 500)
    val summary: String,

    @field:Valid
    val period: PeriodRequest?,

    @field:Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @field:Size(max = 100)
    val role: String?,
)
