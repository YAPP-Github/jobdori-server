package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.experience.dto.request.contents.ExperienceContentsRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class UpdateExperienceRequest(
    @field:Positive
    val projectId: Long? = null,

    @field:Size(max = 10)
    val tags: List<String>? = null,

    @field:Pattern(regexp = "(?s).*\\S.*", message = "must not be blank")
    @field:Size(max = 150)
    val title: String? = null,

    @field:Valid
    val contents: ExperienceContentsRequest? = null,
)
