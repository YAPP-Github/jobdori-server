package com.jobdori.core.domain.resume

import java.math.BigDecimal
import java.time.LocalDateTime

data class ResumeSectionItem(
    val id: Long,
    val sectionId: Long,
    val payload: ResumeSectionItemPayload,
    val displayOrder: BigDecimal,
    val visible: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {

    companion object {
        fun newInstance(
            sectionId: Long,
            payload: ResumeSectionItemPayload,
            displayOrder: BigDecimal = BigDecimal.ZERO,
            visible: Boolean = true,
            now: LocalDateTime = LocalDateTime.now(),
        ) = ResumeSectionItem(
            id = 0L,
            sectionId = sectionId,
            payload = payload,
            displayOrder = displayOrder,
            visible = visible,
            createdAt = now,
            updatedAt = now,
        )
    }

}
