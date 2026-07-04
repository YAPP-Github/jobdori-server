package com.jobdori.core.application.experience.command

import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand

data class ImportedExperienceCommandGroup(
    val project: ExperienceProjectCreateCommand,
    val experiences: List<ExperienceCreateCommand>,
)
