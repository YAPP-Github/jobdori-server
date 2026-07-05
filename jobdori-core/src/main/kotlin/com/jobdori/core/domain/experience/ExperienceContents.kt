package com.jobdori.core.domain.experience

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = StarExperienceContents::class, name = "STAR"),
    JsonSubTypes.Type(value = FreeExperienceContents::class, name = "FREE"),
)
sealed interface ExperienceContents {

    companion object {
        fun star(
            situation: String,
            task: String,
            action: String,
            result: String,
        ) = StarExperienceContents(
            situation = situation,
            task = task,
            action = action,
            result = result,
        )

        fun free(content: String) = FreeExperienceContents(content = content)
    }

}
