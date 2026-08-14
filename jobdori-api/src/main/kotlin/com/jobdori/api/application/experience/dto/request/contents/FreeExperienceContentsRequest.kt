package com.jobdori.api.application.experience.dto.request.contents

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.domain.experience.FreeExperienceContents
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class FreeExperienceContentsRequest(
    @field:NotBlank(message = "내용을 입력해 주세요.")
    @field:Size(max = 2000, message = "입력 가능한 내용의 최대 길이는 {max}자입니다.")
    val content: String = "",
) {

    @JsonIgnore
    fun isValid(): Boolean = content.isNotBlank()

    fun toDomain() = FreeExperienceContents(content = content)

}
