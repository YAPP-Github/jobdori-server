package com.jobdori.core.domain.experience.service.command

import com.jobdori.core.domain.experience.ExperienceContents

data class ExperienceCreateCommand(
    val tags: List<String>,
    val title: String,
    val contents: ExperienceContents,
)
