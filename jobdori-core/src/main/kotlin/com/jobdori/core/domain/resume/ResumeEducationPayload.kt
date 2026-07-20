package com.jobdori.core.domain.resume

import com.jobdori.common.model.Period

data class ResumeEducationPayload(
    val schoolName: String?,
    val major: String?,
    val degree: String?,
    val status: String?,
    val period: Period?,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.EDUCATION
}
