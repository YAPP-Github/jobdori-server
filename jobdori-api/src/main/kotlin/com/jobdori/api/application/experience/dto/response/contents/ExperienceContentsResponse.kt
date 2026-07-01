package com.jobdori.api.application.experience.dto.response.contents

import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceContentsType

data class ExperienceContentsResponse(
    val type: ExperienceContentsType,
    val star: StarExperienceContentsResponse? = null,
    val free: FreeExperienceContentsResponse? = null,
) {

    companion object {
        fun from(contents: ExperienceContents) = ExperienceContentsResponse(
            type = contents.type,
            star = contents.star?.let(StarExperienceContentsResponse::from),
            free = contents.free?.let(FreeExperienceContentsResponse::from),
        )

        fun star(
            situation: String,
            task: String,
            action: String,
            result: String,
        ) = from(
            ExperienceContents.star(
                situation = situation,
                task = task,
                action = action,
                result = result,
            ),
        )

        fun free(content: String) = from(ExperienceContents.free(content))
    }

}
