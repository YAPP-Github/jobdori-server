package com.jobdori.api.application.experience.dto.response.contents

import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.FreeExperienceContents
import com.jobdori.core.domain.experience.StarExperienceContents

sealed interface ExperienceContentsResponse {

    companion object {
        fun from(contents: ExperienceContents): ExperienceContentsResponse {
            return when (contents) {
                is StarExperienceContents -> StarExperienceContentsResponse.from(contents)
                is FreeExperienceContents -> FreeExperienceContentsResponse.from(contents)
            }
        }
    }

}
