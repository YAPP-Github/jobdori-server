package com.jobdori.api.application.resume.dto.response

import com.jobdori.common.time.toInstantAtSystemDefaultZone
import com.jobdori.core.domain.resume.ResumeSectionItem
import java.time.Instant

data class ResumeSectionItemResponse(
    val itemId: Long,
    val displayOrder: Double,
    val visible: Boolean,
    val payload: ResumeSectionItemPayloadResponse,
    val createdAt: Instant,
) {

    companion object {
        fun from(item: ResumeSectionItem) = ResumeSectionItemResponse(
            itemId = item.id,
            displayOrder = item.displayOrder,
            visible = item.visible,
            payload = ResumeSectionItemPayloadResponse.from(item.payload),
            createdAt = item.createdAt.toInstantAtSystemDefaultZone(),
        )
    }

}
