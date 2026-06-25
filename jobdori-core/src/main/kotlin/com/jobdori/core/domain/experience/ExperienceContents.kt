package com.jobdori.core.domain.experience

data class ExperienceContents(
    val type: ExperienceContentsType,
    val star: StarExperienceContents? = null,
    val free: FreeExperienceContents? = null,
) {

    companion object {
        fun star(
            situation: String,
            task: String,
            action: String,
            result: String,
        ) = ExperienceContents(
            type = ExperienceContentsType.STAR,
            star = StarExperienceContents(
                situation = situation,
                task = task,
                action = action,
                result = result,
            ),
        )

        fun free(content: String) = ExperienceContents(
            type = ExperienceContentsType.FREE,
            free = FreeExperienceContents(content = content),
        )
    }

}
