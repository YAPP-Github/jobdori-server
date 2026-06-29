package com.jobdori.infrastructure.persistence.workspace.repository

import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.repository.WorkspaceRepository
import com.jobdori.infrastructure.persistence.workspace.entity.WorkspaceEntity
import org.springframework.stereotype.Repository

@Repository
class WorkspaceRepositoryImpl(
    private val jpaRepository: WorkspaceJpaRepository,
) : WorkspaceRepository {

    override fun findByPublicId(publicId: String): Workspace? {
        return jpaRepository.findByPublicId(publicId)
            ?.toDomain()
    }

    override fun findAllByOwnerUserId(ownerUserId: Long): List<Workspace> {
        return jpaRepository.findAllByOwnerUserId(ownerUserId)
            .map { it.toDomain() }
    }

    override fun save(workspace: Workspace): Workspace {
        val entity = jpaRepository.save(WorkspaceEntity.from(workspace))
        return entity.toDomain()
    }

}
