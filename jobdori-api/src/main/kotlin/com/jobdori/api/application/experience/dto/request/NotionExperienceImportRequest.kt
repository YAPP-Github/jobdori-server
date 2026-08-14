package com.jobdori.api.application.experience.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class NotionExperienceImportRequest(
    @field:Positive(message = "올바른 노션 연결 ID를 입력해 주세요.")
    val connectionId: Long = 0,

    @field:NotBlank(message = "노션 페이지 ID를 입력해 주세요.")
    val pageId: String = "",
)
