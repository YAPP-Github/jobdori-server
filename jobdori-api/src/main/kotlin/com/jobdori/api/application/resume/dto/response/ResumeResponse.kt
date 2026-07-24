package com.jobdori.api.application.resume.dto.response

import com.jobdori.api.application.jd.dto.response.JdResponse
import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.common.time.toInstantAtSystemDefaultZone
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.ResumeTemplate
import java.time.Instant

data class ResumeResponse(
    val resumeId: Long,
    val targetJd: JdResponse?,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    val coreCompetencyGenerated: Boolean,
    val sections: List<ResumeSectionResponse>,
    val createdAt: Instant,
) {

    companion object {
        fun from(
            resume: Resume,
            targetJd: JdResponse?,
        ) = ResumeResponse(
            resumeId = resume.id,
            targetJd = targetJd,
            template = resume.template,
            status = ResumeStatusType.from(resume.status),
            coreCompetencyGenerated = resume.coreCompetencyGenerated,
            sections = emptyList(),
            createdAt = resume.createdAt.toInstantAtSystemDefaultZone(),
        )

        fun from(
            detail: ResumeDetail,
            targetJd: JdResponse?,
        ) = ResumeResponse(
            resumeId = detail.resume.id,
            targetJd = targetJd,
            template = detail.resume.template,
            status = ResumeStatusType.from(detail.resume.status),
            coreCompetencyGenerated = detail.resume.coreCompetencyGenerated,
            sections = detail.sections
                .sortedBy { it.section.displayOrder }
                .map { ResumeSectionResponse.from(it.section, it.items) },
            createdAt = detail.resume.createdAt.toInstantAtSystemDefaultZone(),
        )
    }

}
