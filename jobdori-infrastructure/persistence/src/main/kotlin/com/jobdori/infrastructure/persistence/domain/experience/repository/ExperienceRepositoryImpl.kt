package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceEntity
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ExperienceRepositoryImpl(
    private val jpaRepository: ExperienceJpaRepository,
) : ExperienceRepository {

    @Transactional
    override fun save(experience: Experience): Experience {
        return jpaRepository.save(ExperienceEntity.from(experience)).toDomain()
    }

    @Transactional
    override fun saveAll(experiences: List<Experience>): List<Experience> {
        if (experiences.isEmpty()) {
            return emptyList()
        }

        return jpaRepository.saveAll(experiences.map { ExperienceEntity.from(it) })
            .map { it.toDomain() }
    }

    @Transactional
    override fun updateStatusByWorkspaceIdAndProjectId(
        workspaceId: Long,
        projectId: Long,
        status: ExperienceStatus,
    ) {
        jpaRepository.updateStatusByWorkspaceIdAndProjectId(
            workspaceId = workspaceId,
            projectId = projectId,
            status = status,
        )
    }

    @Transactional(readOnly = true)
    override fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): Experience? {
        return jpaRepository.findByIdAndWorkspaceIdAndStatus(
            id = id,
            workspaceId = workspaceId,
            status = ExperienceStatus.ACTIVE,
        )?.toDomain()
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    override fun searchAllByWorkspaceId(
        workspaceId: Long,
        keyword: String,
        cursorId: Long?,
        size: Int,
    ): List<Experience> {
        val pageable = PageRequest.of(0, size)
        val escapedKeyword = escapeLikeWildcards(keyword.trim().lowercase())
        val keywordPattern = "%${escapedKeyword}%"
        val entities = jpaRepository.searchAllByWorkspaceIdAndStatus(
            workspaceId = workspaceId,
            status = ExperienceStatus.ACTIVE,
            keywordPattern = keywordPattern,
            cursorId = cursorId,
            pageable = pageable,
        )

        return entities.map { it.toDomain() }
    }

    private fun escapeLikeWildcards(keyword: String): String {
        return keyword
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }

    @Transactional(readOnly = true)
    override fun findAllActiveByWorkspaceId(workspaceId: Long): List<Experience> =
        jpaRepository.findAllByWorkspaceIdAndStatus(workspaceId, ExperienceStatus.ACTIVE)
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun experienceSignature(workspaceId: Long): String {
        val view = jpaRepository.signatureView(workspaceId, ExperienceStatus.ACTIVE)
        return "${view.cnt}:${view.maxUpdatedAt}"
    }

    @Transactional(readOnly = true)
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
        )
    }

}
