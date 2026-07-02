package com.jobdori.core.domain.experience.service.command

import com.jobdori.common.model.Period

data class ExperienceProjectCreateCommand(
    val name: String,
    val summary: String,
    val period: Period?,
    val role: String?,
)
