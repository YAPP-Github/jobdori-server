package com.jobdori.core.domain.experience.service.command

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.ExperienceContents

data class ExperienceCreateCommand(
    val tags: List<String>,
    val title: String,
    val contents: ExperienceContents,
    val period: Period? = null,
    val role: String? = null,
)
