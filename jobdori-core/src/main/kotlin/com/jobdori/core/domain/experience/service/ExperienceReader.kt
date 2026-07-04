package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.SliceResult
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.error.ExperienceNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceReader(
    private val experienceRepository: ExperienceRepository,
) {

    @Transactional(readOnly = true)
    fun getExperience(workspaceId: Long, experienceId: Long): Experience {
        return experienceRepository.findByIdAndWorkspaceId(experienceId, workspaceId)
            ?: throw ExperienceNotFoundException(
                "존재하지 않는 경험입니다. [workspaceId=$workspaceId, experienceId=$experienceId]",
            )
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

}
