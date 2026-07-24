package com.jobdori.api.application.resume.dto.response

import com.jobdori.api.application.jd.dto.response.JdResponse
import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.common.time.toInstantAtSystemDefaultZone
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeTemplate
import java.time.Instant

data class ResumeSummaryResponse(
    val resumeId: Long,
    val targetJd: JdResponse?,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    val coreCompetencyGenerated: Boolean,
    val createdAt: Instant,
) {

    companion object {
        fun from(resume: Resume, targetJd: JdResponse?) = ResumeSummaryResponse(
            resumeId = resume.id,
            targetJd = targetJd,
            template = resume.template,
            status = ResumeStatusType.from(resume.status),
            coreCompetencyGenerated = resume.coreCompetencyGenerated,
            createdAt = resume.createdAt.toInstantAtSystemDefaultZone(),
        )
    }

}
