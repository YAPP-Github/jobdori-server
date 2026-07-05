package com.jobdori.api.application.experience.dto.response.contents

import com.jobdori.core.domain.experience.StarExperienceContents

data class StarExperienceContentsResponse(
    val situation: String,
    val task: String,
    val action: String,
    val result: String,
) : ExperienceContentsResponse {

    companion object {
        fun from(contents: StarExperienceContents) = StarExperienceContentsResponse(
            situation = contents.situation,
            task = contents.task,
            action = contents.action,
            result = contents.result,
        )
    }

}
