package com.jobdori.api.application.sample

import jakarta.validation.constraints.NotBlank

data class SampleCreateRequest(
    @field:NotBlank
    val name: String = "",
)
