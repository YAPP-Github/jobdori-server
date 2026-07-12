package com.jobdori.api.application.experience.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class NotionExperienceImportRequest(
    @field:Positive
    val connectionId: Long = 0,

    @field:NotBlank
    val pageId: String = "",
)
