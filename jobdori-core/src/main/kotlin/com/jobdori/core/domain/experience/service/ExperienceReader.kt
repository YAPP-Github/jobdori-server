package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.SliceResult
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.error.ExperienceNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import org.springframework.stereotype.Service

@Service
class ExperienceReader(
    private val experienceRepository: ExperienceRepository,
) {

    fun getExperience(workspaceId: Long, experienceId: Long): Experience {
        return experienceRepository.findByIdAndWorkspaceId(experienceId, workspaceId)
            ?: throw ExperienceNotFoundException(
                "존재하지 않는 경험입니다. [workspaceId=$workspaceId, experienceId=$experienceId]",
            )
    }

    fun getExperiences(
        workspaceId: Long,
        projectId: Long?,
        cursor: String?,
        size: Int,
    ): SliceResult<Experience> {
        val experiences = if (projectId == null) {
            experienceRepository.findAllByWorkspaceId(
                workspaceId = workspaceId,
                cursorId = cursor?.toLongOrNull(),
                size = size + 1,
            )
        } else {
            experienceRepository.findAllByWorkspaceIdAndProjectId(
                workspaceId = workspaceId,
                projectId = projectId,
                cursorId = cursor?.toLongOrNull(),
                size = size + 1,
            )
        }
        val page = experiences.take(size)

        return SliceResult(
            items = page,
            nextCursor = if (experiences.size > size) page.lastOrNull()?.id?.toString() else null,
        )
    }

    fun searchExperiences(
        workspaceId: Long,
        keyword: String,
        cursor: String?,
        size: Int,
    ): SliceResult<Experience> {
        val experiences = experienceRepository.searchAllByWorkspaceId(
            workspaceId = workspaceId,
            keyword = keyword,
            cursorId = cursor?.toLongOrNull(),
            size = size + 1,
        )
        val page = experiences.take(size)

        return SliceResult(
            items = page,
            nextCursor = if (experiences.size > size) page.lastOrNull()?.id?.toString() else null,
        )
    }

    fun findAllActive(workspaceId: Long): List<Experience> =
        experienceRepository.findAllActiveByWorkspaceId(workspaceId)

    fun signature(workspaceId: Long): String =
        experienceRepository.experienceSignature(workspaceId)

    fun getExperienceCountsByProjectIds(workspaceId: Long, projectIds: Collection<Long>): Map<Long, Long> {
        if (projectIds.isEmpty()) {
            return emptyMap()
        }

        return experienceRepository.countByWorkspaceIdAndProjectIds(
            workspaceId = workspaceId,
            projectIds = projectIds.toSet(),
        )
    }

}
