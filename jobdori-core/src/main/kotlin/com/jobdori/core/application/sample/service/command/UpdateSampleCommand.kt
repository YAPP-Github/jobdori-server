package com.jobdori.core.application.sample.service.command

data class UpdateSampleCommand(
    val sampleId: Long,
    val name: String,
)
