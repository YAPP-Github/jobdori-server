package com.jobdori.api.application.experience.dto.response

import com.jobdori.api.application.experience.dto.response.contents.ExperienceContentsResponse
import com.jobdori.core.domain.experience.Experience

data class ExperienceResponse(
    val experienceId: Long,
    val project: ExperienceProjectResponse?,
    val tags: List<String>,
    val title: String,
    val contents: ExperienceContentsResponse,
) {

    companion object {
        fun from(
            experience: Experience,
            project: ExperienceProjectResponse?,
        ) = ExperienceResponse(
            experienceId = experience.id,
            project = project,
            tags = experience.tags,
            title = experience.title,
            contents = ExperienceContentsResponse.from(experience.contents),
        )
    }

}
