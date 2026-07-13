package com.jobdori.core.domain.experience

import com.jobdori.common.model.Period

object ExperienceProjectFixture {

    fun create(
        id: Long = 0L,
        workspaceId: Long = 1L,
        name: String = "프로젝트",
        summary: String = "프로젝트 요약",
        period: Period? = null,
        role: String? = "백엔드 개발자",
        displayOrder: Double = 0.0,
        status: ExperienceProjectStatus = ExperienceProjectStatus.ACTIVE,
    ) = ExperienceProject(
        id = id,
        workspaceId = workspaceId,
        name = name,
        summary = summary,
        period = period,
        role = role,
        displayOrder = displayOrder,
        status = status,
    )

}
