package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceProjectCreator(
    private val experienceProjectRepository: ExperienceProjectRepository,
) {

    @Transactional
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

    @Transactional
    fun create(
        workspaceId: Long,
        commands: List<ExperienceProjectCreateCommand>,
    ): List<ExperienceProject> {
        return commands.map { command ->
            create(
                workspaceId = workspaceId,
                command = command,
            )
        }
    }

}
