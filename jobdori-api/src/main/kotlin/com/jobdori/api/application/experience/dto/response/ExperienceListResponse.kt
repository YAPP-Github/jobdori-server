package com.jobdori.api.application.experience.dto.response

import com.jobdori.api.application.common.dto.response.CursorResponse

data class ExperienceListResponse(
    val experiences: List<ExperienceResponse>,
    val strategy: String? = null,
    val cursor: CursorResponse,
)
