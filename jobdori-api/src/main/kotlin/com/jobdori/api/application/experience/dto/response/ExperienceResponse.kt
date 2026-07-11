package com.jobdori.api.application.experience.dto.response

import com.jobdori.api.application.experience.dto.response.contents.ExperienceContentsResponse
import com.jobdori.core.domain.experience.Experience

data class ExperienceResponse(
    val experienceId: Long,
    val project: ExperienceProjectResponse?,
    val tags: List<String>,
    val title: String,
    val contents: ExperienceContentsResponse,
    // jdId를 준 피드 조회에서만 채워진다. reason은 매칭률 상위 5개만.
    val matchRate: Int? = null,
    val reason: String? = null,
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
            matchRate = matchRate,
            reason = reason,
        )
    }

}
