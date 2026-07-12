package com.jobdori.core.domain.experience

import com.jobdori.common.model.Period

data class ExperienceProject(
    val id: Long,
    val workspaceId: Long,
    val name: String,
    val summary: String,
    val period: Period?,
    val role: String?,
    val displayOrder: Double,
    val status: ExperienceProjectStatus,
) {

    companion object {
        fun newInstance(
            workspaceId: Long,
            name: String,
            summary: String,
            period: Period?,
            role: String?,
            displayOrder: Double = 0.0,
        ) = ExperienceProject(
            id = 0L,
            workspaceId = workspaceId,
            name = name,
            summary = summary,
            period = period,
            role = role,
            displayOrder = displayOrder,
            status = ExperienceProjectStatus.ACTIVE,
        )
    }

}
