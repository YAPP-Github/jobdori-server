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

    @AssertTrue(message = "STAR 형식은 STAR 내용만, FREE 형식은 자유 형식 내용만 입력해 주세요.")
    @JsonIgnore
    fun isValidByType(): Boolean {
        return when (type) {
            ExperienceContentsType.STAR -> star != null && free == null
            ExperienceContentsType.FREE -> free != null && star == null
        }
    }

    fun toDomain(): ExperienceContents {
        return when (type) {
            ExperienceContentsType.STAR -> requireNotNull(star) { "STAR contents require star payload" }.toDomain()
            ExperienceContentsType.FREE -> requireNotNull(free) { "FREE contents require free payload" }.toDomain()
        }
    }

}
