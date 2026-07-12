package com.jobdori.core.domain.resume

import com.jobdori.common.model.Period
import java.time.LocalDate

data class ResumeAwardPayload(
    val name: String,
    val period: Period?,
    val awardedAt: LocalDate?,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.AWARD
}
