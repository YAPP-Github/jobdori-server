package com.jobdori.core.application.experience

import com.jobdori.core.application.experience.command.ImportedExperienceCommandGroup
import com.jobdori.core.domain.experience.service.ExperienceCreator
import com.jobdori.core.domain.experience.service.ExperienceProjectCreator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceImportService(
    private val experienceProjectCreator: ExperienceProjectCreator,
    private val experienceCreator: ExperienceCreator,
) {

    @Transactional
    fun saveAll(workspaceId: Long, groups: List<ImportedExperienceCommandGroup>) {
        if (groups.isEmpty()) {
            return
        }

        val projects = experienceProjectCreator.create(
            workspaceId = workspaceId,
            commands = groups.map { group -> group.project },
        )
        groups.zip(projects).forEach { (group, project) ->
            experienceCreator.create(
                workspaceId = workspaceId,
                projectId = project.id,
                commands = group.experiences,
            )
        }
    }

}
