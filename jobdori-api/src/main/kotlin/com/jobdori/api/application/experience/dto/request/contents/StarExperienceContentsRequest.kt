package com.jobdori.api.application.experience.dto.request.contents

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.domain.experience.StarExperienceContents
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StarExperienceContentsRequest(
    @field:NotBlank(message = "상황을 입력해 주세요.")
    @field:Size(max = 500, message = "입력 가능한 상황의 최대 길이는 {max}자입니다.")
    val situation: String = "",

    @field:NotBlank(message = "과제를 입력해 주세요.")
    @field:Size(max = 500, message = "입력 가능한 과제의 최대 길이는 {max}자입니다.")
    val task: String = "",

    @field:NotBlank(message = "행동을 입력해 주세요.")
    @field:Size(max = 500, message = "입력 가능한 행동의 최대 길이는 {max}자입니다.")
    val action: String = "",

    @field:NotBlank(message = "결과를 입력해 주세요.")
    @field:Size(max = 500, message = "입력 가능한 결과의 최대 길이는 {max}자입니다.")
    val result: String = "",
) {

    @JsonIgnore
    fun isValid(): Boolean = listOf(situation, task, action, result).all { it.isNotBlank() }

    fun toDomain() = StarExperienceContents(
        situation = situation,
        task = task,
        action = action,
        result = result,
    )

}
