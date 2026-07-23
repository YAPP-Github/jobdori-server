package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperiencePolicy
import com.jobdori.core.domain.experience.error.ExperienceProjectLimitExceededException
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
        validateProjectLimit(workspaceId = workspaceId, createCount = 1)

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
        if (commands.isEmpty()) {
            return emptyList()
        }
        validateProjectLimit(workspaceId = workspaceId, createCount = commands.size)

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

    private fun validateProjectLimit(workspaceId: Long, createCount: Int) {
        val projectCount = experienceProjectRepository.countByWorkspaceId(workspaceId)
        if (projectCount + createCount > ExperiencePolicy.MAX_PROJECT_COUNT) {
            throw ExperienceProjectLimitExceededException()
        }
    }

}
