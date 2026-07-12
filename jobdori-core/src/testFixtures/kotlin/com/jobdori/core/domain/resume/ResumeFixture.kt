package com.jobdori.core.domain.resume

import java.time.LocalDateTime

object ResumeFixture {

    fun create(
        id: Long = 0L,
        workspaceId: Long = 1L,
        targetJdId: Long? = null,
        title: String = "이력서",
        template: ResumeTemplate = ResumeTemplate.DEFAULT,
        status: ResumeStatus = ResumeStatus.DRAFT,
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 0, 0),
        updatedAt: LocalDateTime = createdAt,
    ) = Resume(
        id = id,
        workspaceId = workspaceId,
        targetJdId = targetJdId,
        title = title,
        template = template,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

}

