package com.jobdori.core.domain.resume

import java.time.LocalDate

data class ResumeAwardPayload(
    val name: String?,
    val organization: String?,
    val awardedAt: LocalDate?,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.AWARD
}
