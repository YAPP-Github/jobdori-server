package com.jobdori.api.application.sample

import jakarta.validation.constraints.NotBlank

data class SampleUpdateRequest(
    val sampleId: Long,

    @field:NotBlank
    val name: String = "",
)
