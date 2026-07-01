package com.jobdori.core.domain.experience

import com.jobdori.common.model.Period
import java.math.BigDecimal

data class ExperienceProject(
    val id: Long,
    val workspaceId: Long,
    val name: String,
    val summary: String,
    val period: Period?,
    val role: String?,
    val displayOrder: BigDecimal,
    val status: ExperienceProjectStatus,
) {

    companion object {
        fun newInstance(
            workspaceId: Long,
            name: String,
            summary: String,
            period: Period?,
            role: String?,
            displayOrder: BigDecimal = BigDecimal.ZERO,
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
