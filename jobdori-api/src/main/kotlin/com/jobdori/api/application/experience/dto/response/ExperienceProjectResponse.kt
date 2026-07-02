package com.jobdori.api.application.experience.dto.response

import com.jobdori.api.application.common.dto.response.PeriodResponse
import com.jobdori.core.domain.experience.ExperienceProject

data class ExperienceProjectResponse(
    val projectId: Long,
    val name: String,
    val summary: String,
    val period: PeriodResponse?,
    val role: String?,
    val experienceCount: Int? = null,
) {

    companion object {
        fun from(project: ExperienceProject, experienceCount: Int? = null) = ExperienceProjectResponse(
            projectId = project.id,
            name = project.name,
            summary = project.summary,
            period = project.period?.let(PeriodResponse::from),
            role = project.role,
            experienceCount = experienceCount,
        )
    }

}
