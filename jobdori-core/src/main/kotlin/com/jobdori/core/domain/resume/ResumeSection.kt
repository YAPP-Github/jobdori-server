package com.jobdori.core.domain.resume

import java.math.BigDecimal
import java.time.LocalDateTime

data class ResumeSection(
    val id: Long,
    val resumeId: Long,
    val type: ResumeSectionType,
    val displayOrder: BigDecimal,
    val visible: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {

    companion object {
        fun newInstance(
            resumeId: Long,
            type: ResumeSectionType,
            displayOrder: BigDecimal = BigDecimal.ZERO,
            visible: Boolean = true,
            now: LocalDateTime = LocalDateTime.now(),
        ) = ResumeSection(
            id = 0L,
            resumeId = resumeId,
            type = type,
            displayOrder = displayOrder,
            visible = visible,
            createdAt = now,
            updatedAt = now,
        )
    }

}
