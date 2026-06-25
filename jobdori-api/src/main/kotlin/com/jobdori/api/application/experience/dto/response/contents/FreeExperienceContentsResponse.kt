package com.jobdori.api.application.experience.dto.response.contents

import com.jobdori.core.domain.experience.FreeExperienceContents

data class FreeExperienceContentsResponse(
    val content: String,
) {

    companion object {
        fun from(contents: FreeExperienceContents) = FreeExperienceContentsResponse(
            content = contents.content,
        )
    }

}
