package com.jobdori.api.application.resume.dto.response

import com.jobdori.api.application.common.dto.response.CursorResponse

data class ResumeListResponse(
    val resumes: List<ResumeSummaryResponse>,
    val cursor: CursorResponse,
)
