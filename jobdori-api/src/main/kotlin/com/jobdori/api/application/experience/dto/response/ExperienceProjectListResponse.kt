package com.jobdori.api.application.experience.dto.response

import com.jobdori.api.application.common.dto.response.CursorResponse

data class ExperienceProjectListResponse(
    val projects: List<ExperienceProjectResponse>,
    val cursor: CursorResponse,
)
