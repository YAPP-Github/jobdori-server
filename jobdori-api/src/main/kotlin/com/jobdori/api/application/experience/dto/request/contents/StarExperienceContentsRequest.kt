package com.jobdori.api.application.experience.dto.request.contents

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.domain.experience.StarExperienceContents
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class StarExperienceContentsRequest(
    @field:NotBlank
    @field:Size(max = 1000)
    val situation: String = "",

    @field:NotBlank
    @field:Size(max = 1000)
    val task: String = "",

    @field:NotBlank
    @field:Size(max = 1000)
    val action: String = "",

    @field:NotBlank
    @field:Size(max = 1000)
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
