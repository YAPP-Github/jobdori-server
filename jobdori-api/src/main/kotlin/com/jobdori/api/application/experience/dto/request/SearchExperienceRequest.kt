package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.common.dto.request.CursorRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class SearchExperienceRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val keyword: String,

    override val cursor: String? = null,

    @field:Max(value = 30)
    @field:Positive
    override val size: Int = 10,
) : CursorRequest
