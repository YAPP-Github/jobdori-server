package com.jobdori.api.application.experience.service

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.experience.dto.response.ExperienceListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.service.ExperienceCreator
import com.jobdori.core.domain.experience.service.ExperienceModifier
import com.jobdori.core.domain.experience.service.ExperienceProjectReader
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experience.service.ExperienceRemover
import org.springframework.stereotype.Service

@Service
class ExperienceService(
    private val experienceCreator: ExperienceCreator,
    private val experienceReader: ExperienceReader,
    private val experienceModifier: ExperienceModifier,
    private val experienceRemover: ExperienceRemover,
    private val experienceProjectReader: ExperienceProjectReader,
) {

    fun create(
        workspaceId: Long,
        projectId: Long,
        tags: List<String>,
        title: String,
        contents: ExperienceContents,
    ): ExperienceResponse {
        val project = experienceProjectReader.getProject(workspaceId, projectId)
        val experience = experienceCreator.create(
            workspaceId = workspaceId,
            projectId = projectId,
            tags = tags,
            title = title,
            contents = contents,
        )

        return ExperienceResponse.from(
            experience = experience,
            project = ExperienceProjectResponse.from(project),
        )
    }

    fun get(
        workspaceId: Long,
        experienceId: Long,
        includeProject: Boolean,
    ): ExperienceResponse {
        val experience = experienceReader.getExperience(
            workspaceId = workspaceId,
            experienceId = experienceId,
        )
        val project = if (includeProject) {
            ExperienceProjectResponse.from(
                experienceProjectReader.getProject(workspaceId, experience.projectId),
            )
        } else {
            null
        }

        return ExperienceResponse.from(
            experience = experience,
            project = project,
        )
    }

    fun getAll(
        workspaceId: Long,
        projectId: Long?,
        cursor: String?,
        size: Int,
        includeProjects: Boolean,
    ): ExperienceListResponse {
        if (projectId != null) {
            experienceProjectReader.getProject(workspaceId, projectId)
        }

        val result = experienceReader.getExperiences(
            workspaceId = workspaceId,
            projectId = projectId,
            cursor = cursor,
            size = size,
        )
        val projects = if (includeProjects) {
            experienceProjectReader.getProjects(
                workspaceId = workspaceId,
                projectIds = result.items.map { it.projectId },
            ).mapValues { (_, project) -> ExperienceProjectResponse.from(project) }
        } else {
            emptyMap()
        }

        return ExperienceListResponse(
            experiences = result.items.map { experience ->
                ExperienceResponse.from(
                    experience = experience,
                    project = projects[experience.projectId],
                )
            },
            cursor = CursorResponse(nextCursor = result.nextCursor),
        )
    }

    fun modify(
        workspaceId: Long,
        experienceId: Long,
        projectId: Long?,
        tags: List<String>?,
        title: String?,
        contents: ExperienceContents?,
    ): ExperienceResponse {
        if (projectId != null) {
            experienceProjectReader.getProject(
                workspaceId = workspaceId,
                projectId = projectId,
            )
        }

        val modified = experienceModifier.modify(
            workspaceId = workspaceId,
            experienceId = experienceId,
            projectId = projectId,
            tags = tags,
            title = title,
            contents = contents,
        )
        val project = experienceProjectReader.getProject(
            workspaceId = workspaceId,
            projectId = modified.projectId,
        )

        return ExperienceResponse.from(
            experience = modified,
            project = ExperienceProjectResponse.from(project),
        )
    }

    fun remove(workspaceId: Long, experienceId: Long) {
        experienceRemover.remove(
            workspaceId = workspaceId,
            experienceId = experienceId,
        )
    }

}
