package com.jobdori.api.application.resume.dto.response

import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.common.time.toInstantAtSystemDefaultZone
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeTemplate
import java.time.Instant

data class ResumeSummaryResponse(
    val resumeId: Long,
    val targetJdId: String?,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    val createdAt: Instant,
    val updatedAt: Instant,
) {

    companion object {
        fun from(resume: Resume) = ResumeSummaryResponse(
            resumeId = resume.id,
            targetJdId = resume.targetJdId.toString(),
            template = resume.template,
            status = ResumeStatusType.from(resume.status),
            createdAt = resume.createdAt.toInstantAtSystemDefaultZone(),
            updatedAt = resume.updatedAt.toInstantAtSystemDefaultZone(),
        )
    }

}
