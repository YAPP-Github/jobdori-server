package com.jobdori.core.domain.experience

import com.jobdori.common.model.Period

data class Experience(
    val id: Long,
    val workspaceId: Long,
    val projectId: Long,
    val tags: List<String>,
    val title: String,
    val contents: ExperienceContents,
    val period: Period? = null,
    val role: String? = null,
    val displayOrder: Double,
    val status: ExperienceStatus,
) {

    companion object {
        fun newInstance(
            workspaceId: Long,
            projectId: Long,
            tags: List<String>,
            title: String,
            contents: ExperienceContents,
            period: Period? = null,
            role: String? = null,
            displayOrder: Double = 0.0,
        ) = Experience(
            id = 0L,
            workspaceId = workspaceId,
            projectId = projectId,
            tags = tags,
            title = title,
            contents = contents,
            period = period,
            role = role,
            displayOrder = displayOrder,
            status = ExperienceStatus.ACTIVE,
        )
    }

}
