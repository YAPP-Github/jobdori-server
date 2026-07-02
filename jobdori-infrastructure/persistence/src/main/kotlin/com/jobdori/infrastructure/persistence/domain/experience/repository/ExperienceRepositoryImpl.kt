package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceEntity
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class ExperienceRepositoryImpl(
    private val jpaRepository: ExperienceJpaRepository,
) : ExperienceRepository {

    override fun save(experience: Experience): Experience {
        return jpaRepository.save(ExperienceEntity.from(experience)).toDomain()
    }

    override fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): Experience? {
        return jpaRepository.findByIdAndWorkspaceIdAndStatus(
            id = id,
            workspaceId = workspaceId,
            status = ExperienceStatus.ACTIVE,
        )?.toDomain()
    }

    override fun findAllByWorkspaceId(
        workspaceId: Long,
        cursorId: Long?,
        size: Int,
    ): List<Experience> {
        val pageable = PageRequest.of(0, size)
        val entities = if (cursorId == null) {
            jpaRepository.findAllByWorkspaceIdAndStatusOrderByIdDesc(
                workspaceId = workspaceId,
                status = ExperienceStatus.ACTIVE,
                pageable = pageable,
            )
        } else {
            jpaRepository.findAllByWorkspaceIdAndStatusAndIdLessThanOrderByIdDesc(
                workspaceId = workspaceId,
                status = ExperienceStatus.ACTIVE,
                id = cursorId,
                pageable = pageable,
            )
        }

        return entities.map { it.toDomain() }
    }

    override fun findAllByWorkspaceIdAndProjectId(
        workspaceId: Long,
        projectId: Long,
        cursorId: Long?,
        size: Int,
    ): List<Experience> {
        val pageable = PageRequest.of(0, size)
        val entities = if (cursorId == null) {
            jpaRepository.findAllByWorkspaceIdAndProjectIdAndStatusOrderByIdDesc(
                workspaceId = workspaceId,
                projectId = projectId,
                status = ExperienceStatus.ACTIVE,
                pageable = pageable,
            )
        } else {
            jpaRepository.findAllByWorkspaceIdAndProjectIdAndStatusAndIdLessThanOrderByIdDesc(
                workspaceId = workspaceId,
                projectId = projectId,
                status = ExperienceStatus.ACTIVE,
                id = cursorId,
                pageable = pageable,
            )
        }

        return entities.map { it.toDomain() }
    }

    override fun searchAllByWorkspaceId(
        workspaceId: Long,
        keyword: String,
        cursorId: Long?,
        size: Int,
    ): List<Experience> {
        val pageable = PageRequest.of(0, size)
        val keywordPattern = "%${keyword.trim().lowercase()}%"
        val entities = jpaRepository.searchAllByWorkspaceIdAndStatus(
            workspaceId = workspaceId,
            status = ExperienceStatus.ACTIVE,
            keywordPattern = keywordPattern,
            cursorId = cursorId,
            pageable = pageable,
        )

        return entities.map { it.toDomain() }
    }

    override fun countByWorkspaceIdAndProjectIds(
        workspaceId: Long,
        projectIds: Collection<Long>,
    ): Map<Long, Long> {
        if (projectIds.isEmpty()) {
            return emptyMap()
        }

        return jpaRepository.countByWorkspaceIdAndProjectIdsAndStatus(
            workspaceId = workspaceId,
            projectIds = projectIds,
            status = ExperienceStatus.ACTIVE,
        ).associate { it.projectId to it.experienceCount }
    }

}
