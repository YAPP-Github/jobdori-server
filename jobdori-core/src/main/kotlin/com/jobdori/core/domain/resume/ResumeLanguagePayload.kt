package com.jobdori.core.domain.resume

import java.time.LocalDate

data class ResumeLanguagePayload(
    val examName: String?,
    val scoreOrGrade: String?,
    val acquiredAt: LocalDate?,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.LANGUAGE
}
