package com.jobdori.api.application.experience.dto.response.contents

import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceContentsType
import com.jobdori.core.domain.experience.FreeExperienceContents
import com.jobdori.core.domain.experience.StarExperienceContents

data class ExperienceContentsResponse(
    val type: ExperienceContentsType,
    val star: StarExperienceContentsResponse? = null,
    val free: FreeExperienceContentsResponse? = null,
) {

    companion object {
        fun from(contents: ExperienceContents): ExperienceContentsResponse {
            return when (contents) {
                is StarExperienceContents -> ExperienceContentsResponse(
                    type = ExperienceContentsType.STAR,
                    star = StarExperienceContentsResponse.from(contents),
                )

                is FreeExperienceContents -> ExperienceContentsResponse(
                    type = ExperienceContentsType.FREE,
                    free = FreeExperienceContentsResponse.from(contents),
                )
            }
        }
    }

}
