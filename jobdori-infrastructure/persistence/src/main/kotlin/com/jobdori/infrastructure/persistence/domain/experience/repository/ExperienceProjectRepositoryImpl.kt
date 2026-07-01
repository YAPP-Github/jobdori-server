package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceProjectEntity
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class ExperienceProjectRepositoryImpl(
    private val jpaRepository: ExperienceProjectJpaRepository,
) : ExperienceProjectRepository {

    override fun save(project: ExperienceProject): ExperienceProject {
        return jpaRepository.save(ExperienceProjectEntity.from(project)).toDomain()
    }

    override fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): ExperienceProject? {
        return jpaRepository.findByIdAndWorkspaceIdAndStatus(
            id = id,
            workspaceId = workspaceId,
            status = ExperienceProjectStatus.ACTIVE,
        )?.toDomain()
    }

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
