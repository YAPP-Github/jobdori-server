package com.jobdori.api.application.experience.dto.response

import com.jobdori.api.application.common.dto.response.PeriodResponse
import com.jobdori.api.application.experience.dto.response.contents.ExperienceContentsResponse
import com.jobdori.core.domain.experience.Experience

data class ExperienceResponse(
    val experienceId: Long,
    val project: ExperienceProjectResponse?,
    val tags: List<String>,
    val title: String,
    val contents: ExperienceContentsResponse,
    val period: PeriodResponse?,
    val role: String?,
    val matchRate: Int? = null,
    val recommendedReason: String? = null,
) {

    companion object {
        fun from(
            experience: Experience,
            project: ExperienceProjectResponse?,
            matchRate: Int? = null,
            reason: String? = null,
        ) = ExperienceResponse(
            experienceId = experience.id,
            project = project,
            tags = experience.tags,
            title = experience.title,
            contents = ExperienceContentsResponse.from(experience.contents),
            period = experience.period?.let(PeriodResponse::from),
            role = experience.role,
            matchRate = matchRate,
            recommendedReason = reason,
        )
    }

}
