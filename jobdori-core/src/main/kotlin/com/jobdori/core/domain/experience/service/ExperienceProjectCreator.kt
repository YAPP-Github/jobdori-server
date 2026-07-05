package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import org.springframework.stereotype.Service

@Service
class ExperienceProjectCreator(
    private val experienceProjectRepository: ExperienceProjectRepository,
) {

    fun create(
        workspaceId: Long,
        command: ExperienceProjectCreateCommand,
    ): ExperienceProject {
        return experienceProjectRepository.save(
            ExperienceProject.newInstance(
                workspaceId = workspaceId,
                name = command.name,
                summary = command.summary,
                period = command.period,
                role = command.role,
            ),
        )
    }

    fun create(
        workspaceId: Long,
        commands: List<ExperienceProjectCreateCommand>,
    ): List<ExperienceProject> {
        val projects = commands.map { command ->
            ExperienceProject.newInstance(
                workspaceId = workspaceId,
                name = command.name,
                summary = command.summary,
                period = command.period,
                role = command.role,
            )
        }

        return experienceProjectRepository.saveAll(projects)
    }

}
