package com.jobdori.api.application.resume.dto.response

import com.jobdori.common.time.toInstantAtSystemDefaultZone
import com.jobdori.core.domain.resume.ResumeSection
import com.jobdori.core.domain.resume.ResumeSectionItem
import com.jobdori.core.domain.resume.ResumeSectionType
import java.time.Instant

data class ResumeSectionResponse(
    val sectionId: Long,
    val type: ResumeSectionType,
    val displayText: String,
    val displayOrder: Double,
    val visible: Boolean,
    val items: List<ResumeSectionItemResponse>,
    val createdAt: Instant,
) {

    companion object {
        fun from(section: ResumeSection, items: List<ResumeSectionItem>) = ResumeSectionResponse(
            sectionId = section.id,
            type = section.type,
            displayText = section.type.displayText,
            displayOrder = section.displayOrder,
            visible = section.visible,
            items = items
                .sortedBy { it.displayOrder }
                .map { ResumeSectionItemResponse.from(it) },
            createdAt = section.createdAt.toInstantAtSystemDefaultZone(),
        )
    }

}
