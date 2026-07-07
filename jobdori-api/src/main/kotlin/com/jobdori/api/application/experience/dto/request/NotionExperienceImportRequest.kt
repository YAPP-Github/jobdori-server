package com.jobdori.api.application.experience.dto.request

import jakarta.validation.constraints.NotBlank

data class NotionExperienceImportRequest(
    @field:NotBlank
    val connectionId: String = "",

    @field:NotBlank
    val pageId: String = "",
)
