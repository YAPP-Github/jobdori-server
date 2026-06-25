package com.jobdori.core.domain.experience

import java.math.BigDecimal

data class Experience(
    val id: Long,
    val workspaceId: Long,
    val projectId: Long,
    val tags: List<String>,
    val title: String,
    val contents: ExperienceContents,
    val displayOrder: BigDecimal,
    val status: ExperienceStatus,
) {

    companion object {
        fun newInstance(
            workspaceId: Long,
            projectId: Long,
            tags: List<String>,
            title: String,
            contents: ExperienceContents,
            displayOrder: BigDecimal = BigDecimal.ZERO,
        ) = Experience(
            id = 0L,
            workspaceId = workspaceId,
            projectId = projectId,
            tags = tags,
            title = title,
            contents = contents,
            displayOrder = displayOrder,
            status = ExperienceStatus.ACTIVE,
        )
    }

}
