package com.jobdori.api.application.experience.dto.request.contents

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.domain.experience.FreeExperienceContents
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class FreeExperienceContentsRequest(
    @field:NotBlank(message = "내용을 입력해 주세요.")
    @field:Size(max = 2000, message = "내용은 최대 {max}자까지 입력할 수 있어요.")
    val content: String = "",
) {

    @JsonIgnore
    fun isValid(): Boolean = content.isNotBlank()

    fun toDomain() = FreeExperienceContents(content = content)

}
