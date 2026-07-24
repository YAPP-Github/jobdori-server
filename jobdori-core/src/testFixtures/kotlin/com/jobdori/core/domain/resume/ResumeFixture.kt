package com.jobdori.core.domain.resume

import java.time.LocalDateTime

object ResumeFixture {

    fun create(
        id: Long = 0L,
        workspaceId: Long = 1L,
        targetJdId: Long? = null,
        template: ResumeTemplate = ResumeTemplate.DEFAULT,
        status: ResumeStatus = ResumeStatus.DRAFT,
        coreCompetencyGenerationStatus: CoreCompetencyGenerationStatus =
            CoreCompetencyGenerationStatus.NOT_GENERATED,
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 0, 0),
        updatedAt: LocalDateTime = createdAt,
    ) = Resume(
        id = id,
        workspaceId = workspaceId,
        targetJdId = targetJdId,
        template = template,
        status = status,
        coreCompetencyGenerationStatus = coreCompetencyGenerationStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

}
