package com.jobdori.api.application.experience.dto.request.contents

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceContentsType
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue

data class ExperienceContentsRequest(
    val type: ExperienceContentsType,

    @field:Valid
    val star: StarExperienceContentsRequest? = null,

    @field:Valid
    val free: FreeExperienceContentsRequest? = null,
) {

    @AssertTrue(message = "STAR contents require only star. FREE contents require only free.")
    @JsonIgnore
    fun isValidByType(): Boolean {
        return when (type) {
            ExperienceContentsType.STAR -> star != null && free == null
            ExperienceContentsType.FREE -> free != null && star == null
        }
    }

    fun toDomain() = ExperienceContents(
        type = type,
        star = star?.toDomain(),
        free = free?.toDomain(),
    )

}
