package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.SliceResult
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.error.ExperienceProjectNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceProjectReader(
    private val experienceProjectRepository: ExperienceProjectRepository,
) {

    @Transactional(readOnly = true)
    fun getProject(workspaceId: Long, projectId: Long): ExperienceProject {
        return experienceProjectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
            ?: throw ExperienceProjectNotFoundException(
                "존재하지 않는 경험 프로젝트입니다. [workspaceId=$workspaceId, projectId=$projectId]",
            )
    }

    @Transactional(readOnly = true)
    fun getProjects(workspaceId: Long, projectIds: Collection<Long>): Map<Long, ExperienceProject> {
        if (projectIds.isEmpty()) {
            return emptyMap()
        }

        return experienceProjectRepository.findAllByIdsAndWorkspaceId(
            ids = projectIds.toSet(),
            workspaceId = workspaceId,
        ).associateBy { it.id }
    }

    @Transactional(readOnly = true)
    fun getProjects(workspaceId: Long, cursor: String?, size: Int): SliceResult<ExperienceProject> {
        val projects = experienceProjectRepository.findAllByWorkspaceId(
            workspaceId = workspaceId,
            cursorId = cursor?.toLongOrNull(),
            size = size + 1,
        )
        val page = projects.take(size)

        return SliceResult(
            items = page,
            nextCursor = if (projects.size > size) page.lastOrNull()?.id?.toString() else null,
        )
    }

}
