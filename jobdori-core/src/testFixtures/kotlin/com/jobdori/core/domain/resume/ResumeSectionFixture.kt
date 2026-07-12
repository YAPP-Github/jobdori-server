package com.jobdori.core.domain.resume

import java.math.BigDecimal
import java.time.LocalDateTime

object ResumeSectionFixture {

    fun create(
        id: Long = 0L,
        resumeId: Long = 1L,
        type: ResumeSectionType = ResumeSectionType.BASIC_INFO,
        displayOrder: BigDecimal = BigDecimal.ZERO,
        visible: Boolean = true,
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 0, 0),
        updatedAt: LocalDateTime = createdAt,
    ) = ResumeSection(
        id = id,
        resumeId = resumeId,
        type = type,
        displayOrder = displayOrder,
        visible = visible,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

}

