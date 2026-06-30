package com.jobdori.api.application.experience.dto.request.contents

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.domain.experience.FreeExperienceContents
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class FreeExperienceContentsRequest(
    @field:NotBlank
    @field:Size(max = 3000)
    val content: String = "",
) {

    @JsonIgnore
    fun isValid(): Boolean = content.isNotBlank()

    fun toDomain() = FreeExperienceContents(content = content)

}
