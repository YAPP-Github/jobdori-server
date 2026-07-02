package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceCreator(
    private val experienceRepository: ExperienceRepository,
) {

    @Transactional
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
            ),
        )
    }

    @Transactional
    fun create(
        workspaceId: Long,
        projectId: Long,
        commands: List<ExperienceCreateCommand>,
    ): List<Experience> {
        return commands.map { command ->
            create(
                workspaceId = workspaceId,
                projectId = projectId,
                command = command,
            )
        }
    }

}
