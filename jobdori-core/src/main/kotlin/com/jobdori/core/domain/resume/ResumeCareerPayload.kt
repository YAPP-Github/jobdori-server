package com.jobdori.core.domain.resume

import com.jobdori.common.model.Period

data class ResumeCareerPayload(
    val companyName: String,
    val role: String?,
    val period: Period?,
    val contents: String,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.CAREER
}
