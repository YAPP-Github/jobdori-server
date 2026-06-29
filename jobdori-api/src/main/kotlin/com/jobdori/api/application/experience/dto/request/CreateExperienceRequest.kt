package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.experience.dto.request.contents.ExperienceContentsRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateExperienceRequest(
    @field:Positive
    val projectId: Long,

    @field:Size(max = 10)
    val tags: List<String> = emptyList(),

    @field:NotBlank
    @field:Size(max = 150)
    val title: String = "",

    @field:Valid
    val contents: ExperienceContentsRequest,
)
