package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import org.springframework.stereotype.Service

@Service
class ExperienceCreator(
    private val experienceRepository: ExperienceRepository,
) {

    fun create(
        workspaceId: Long,
        projectId: Long,
        command: ExperienceCreateCommand,
    ): Experience {
        return experienceRepository.save(
            Experience.newInstance(
                workspaceId = workspaceId,
                projectId = projectId,
                tags = command.tags,
                title = command.title,
                contents = command.contents,
                period = command.period,
                role = command.role,
            ),
        )
    }

    fun create(
        workspaceId: Long,
        projectId: Long,
        commands: List<ExperienceCreateCommand>,
    ): List<Experience> {
        val experiences = commands.map { command ->
            Experience.newInstance(
                workspaceId = workspaceId,
                projectId = projectId,
                tags = command.tags,
                title = command.title,
                contents = command.contents,
                period = command.period,
                role = command.role,
            )
        }

        return experienceRepository.saveAll(experiences)
    }

}
