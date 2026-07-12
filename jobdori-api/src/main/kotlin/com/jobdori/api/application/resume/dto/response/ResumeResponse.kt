package com.jobdori.api.application.resume.dto.response

import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.common.time.toInstantAtSystemDefaultZone
import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeSection
import com.jobdori.core.domain.resume.ResumeSectionItem
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeTemplate
import java.time.Instant

data class ResumeResponse(
    val resumeId: Long,
    val targetJdId: Long?,
    val title: String,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    val sections: List<ResumeSectionResponse>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {

    companion object {
        fun from(resume: Resume) = ResumeResponse(
            resumeId = resume.id,
            targetJdId = resume.targetJdId,
            title = resume.title,
            template = resume.template,
            status = ResumeStatusType.from(resume.status),
            sections = emptyList(),
            createdAt = resume.createdAt.toInstantAtSystemDefaultZone(),
            updatedAt = resume.updatedAt.toInstantAtSystemDefaultZone(),
        )

        fun from(detail: ResumeDetail) = ResumeResponse(
            resumeId = detail.resume.id,
            targetJdId = detail.resume.targetJdId,
            title = detail.resume.title,
            template = detail.resume.template,
            status = ResumeStatusType.from(detail.resume.status),
            sections = detail.sections
                .sortedBy { it.section.displayOrder }
                .map { ResumeSectionResponse.from(it.section, it.items) },
            createdAt = detail.resume.createdAt.toInstantAtSystemDefaultZone(),
            updatedAt = detail.resume.updatedAt.toInstantAtSystemDefaultZone(),
        )
    }

}

data class ResumeSummaryResponse(
    val resumeId: Long,
    val targetJdId: Long?,
    val title: String,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    val createdAt: Instant,
    val updatedAt: Instant,
) {

    companion object {
        fun from(resume: Resume) = ResumeSummaryResponse(
            resumeId = resume.id,
            targetJdId = resume.targetJdId,
            title = resume.title,
            template = resume.template,
            status = ResumeStatusType.from(resume.status),
            createdAt = resume.createdAt.toInstantAtSystemDefaultZone(),
            updatedAt = resume.updatedAt.toInstantAtSystemDefaultZone(),
        )
    }

}

data class ResumeSectionResponse(
    val sectionId: Long,
    val type: ResumeSectionType,
    val displayText: String,
    val displayOrder: Double,
    val visible: Boolean,
    val items: List<ResumeSectionItemResponse>,
    val createdAt: Instant,
    val updatedAt: Instant,
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
            updatedAt = section.updatedAt.toInstantAtSystemDefaultZone(),
        )
    }

}

data class ResumeSectionItemResponse(
    val itemId: Long,
    val displayOrder: Double,
    val visible: Boolean,
    val payload: ResumeSectionItemPayloadResponse,
    val createdAt: Instant,
    val updatedAt: Instant,
) {

    companion object {
        fun from(item: ResumeSectionItem) = ResumeSectionItemResponse(
            itemId = item.id,
            displayOrder = item.displayOrder,
            visible = item.visible,
            payload = ResumeSectionItemPayloadResponse.from(item.payload),
            createdAt = item.createdAt.toInstantAtSystemDefaultZone(),
            updatedAt = item.updatedAt.toInstantAtSystemDefaultZone(),
        )
    }

}
