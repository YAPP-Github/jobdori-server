package com.jobdori.core.domain.resume

import java.time.LocalDateTime

data class Resume(
    val id: Long,
    val workspaceId: Long,
    val targetJdId: Long?,
    val title: String,
    val template: ResumeTemplate,
    val status: ResumeStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {

    companion object {
        fun newInstance(
            workspaceId: Long,
            targetJdId: Long?,
        title: String,
        template: ResumeTemplate = ResumeTemplate.DEFAULT,
        now: LocalDateTime = LocalDateTime.now(),
        ) = Resume(
            id = 0L,
            workspaceId = workspaceId,
            targetJdId = targetJdId,
            title = title,
            template = template,
            status = ResumeStatus.DRAFT,
            createdAt = now,
            updatedAt = now,
        )
    }

}
