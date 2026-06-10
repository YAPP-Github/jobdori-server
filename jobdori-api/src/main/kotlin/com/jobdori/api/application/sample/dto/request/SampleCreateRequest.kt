package com.jobdori.api.application.sample.dto.request

import jakarta.validation.constraints.NotBlank

data class SampleCreateRequest(
    @field:NotBlank
    val name: String = "",
)
