package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperiencePolicy
import com.jobdori.core.domain.experience.error.ExperienceLimitExceededException
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
        validateExperienceLimit(
            workspaceId = workspaceId,
            projectId = projectId,
            createCount = 1,
        )

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
        if (commands.isEmpty()) {
            return emptyList()
        }
        validateExperienceLimit(
            workspaceId = workspaceId,
            projectId = projectId,
            createCount = commands.size,
        )

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

    private fun validateExperienceLimit(
        workspaceId: Long,
        projectId: Long,
        createCount: Int,
    ) {
        val experienceCount = experienceRepository.countByWorkspaceIdAndProjectIds(
            workspaceId = workspaceId,
            projectIds = listOf(projectId),
        )[projectId] ?: 0L
        if (experienceCount + createCount > ExperiencePolicy.MAX_EXPERIENCE_COUNT_PER_PROJECT) {
            throw ExperienceLimitExceededException()
        }
    }

}
