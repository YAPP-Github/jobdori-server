package com.jobdori.core.domain.resume

import java.time.LocalDateTime

data class Resume(
    val id: Long,
    val workspaceId: Long,
    val targetJdId: Long?,
    val template: ResumeTemplate,
    val status: ResumeStatus,
    val coreCompetencyGenerationStatus: CoreCompetencyGenerationStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {

    val coreCompetencyGenerated: Boolean
        get() = coreCompetencyGenerationStatus == CoreCompetencyGenerationStatus.GENERATED

    companion object {
        fun newInstance(
            workspaceId: Long,
            targetJdId: Long?,
            template: ResumeTemplate = ResumeTemplate.DEFAULT,
            now: LocalDateTime = LocalDateTime.now(),
        ) = Resume(
            id = 0L,
            workspaceId = workspaceId,
            targetJdId = targetJdId,
            template = template,
            status = ResumeStatus.DRAFT,
            coreCompetencyGenerationStatus = CoreCompetencyGenerationStatus.NOT_GENERATED,
            createdAt = now,
            updatedAt = now,
        )
    }

}
