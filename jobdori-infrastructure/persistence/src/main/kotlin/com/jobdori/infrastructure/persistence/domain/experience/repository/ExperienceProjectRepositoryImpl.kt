package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceProjectEntity
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ExperienceProjectRepositoryImpl(
    private val jpaRepository: ExperienceProjectJpaRepository,
) : ExperienceProjectRepository {

    @Transactional
    override fun save(project: ExperienceProject): ExperienceProject {
        return jpaRepository.save(ExperienceProjectEntity.from(project)).toDomain()
    }

    @Transactional
    override fun saveAll(projects: List<ExperienceProject>): List<ExperienceProject> {
        if (projects.isEmpty()) {
            return emptyList()
        }

        return jpaRepository.saveAll(projects.map { ExperienceProjectEntity.from(it) })
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): ExperienceProject? {
        return jpaRepository.findByIdAndWorkspaceIdAndStatus(
            id = id,
            workspaceId = workspaceId,
            status = ExperienceProjectStatus.ACTIVE,
        )?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAllByIdsAndWorkspaceId(
        ids: Collection<Long>,
        workspaceId: Long,
    ): List<ExperienceProject> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        return jpaRepository.findAllByIdInAndWorkspaceIdAndStatus(
            ids = ids,
            workspaceId = workspaceId,
            status = ExperienceProjectStatus.ACTIVE,
        ).map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findAllByWorkspaceId(
        workspaceId: Long,
        cursorId: Long?,
        size: Int,
    ): List<ExperienceProject> {
        val pageable = PageRequest.of(0, size)
        val entities = if (cursorId == null) {
            jpaRepository.findAllByWorkspaceIdAndStatusOrderByIdDesc(
                workspaceId = workspaceId,
                status = ExperienceProjectStatus.ACTIVE,
                pageable = pageable,
            )
        } else {
            jpaRepository.findAllByWorkspaceIdAndStatusAndIdLessThanOrderByIdDesc(
                workspaceId = workspaceId,
                status = ExperienceProjectStatus.ACTIVE,
                id = cursorId,
                pageable = pageable,
            )
        }

        return entities.map { it.toDomain() }
    }

}
