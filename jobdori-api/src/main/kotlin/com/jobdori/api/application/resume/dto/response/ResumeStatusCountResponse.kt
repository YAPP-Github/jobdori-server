package com.jobdori.api.application.resume.dto.response

import com.jobdori.api.application.resume.dto.ResumeStatusType

data class ResumeStatusCountResponse(
    val status: ResumeStatusType,
    val count: Long,
)
