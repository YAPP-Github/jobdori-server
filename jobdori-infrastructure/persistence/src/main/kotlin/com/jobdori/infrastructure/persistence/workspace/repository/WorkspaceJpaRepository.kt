package com.jobdori.infrastructure.persistence.workspace.repository

import com.jobdori.infrastructure.persistence.workspace.entity.WorkspaceEntity
import org.springframework.data.jpa.repository.JpaRepository

interface WorkspaceJpaRepository : JpaRepository<WorkspaceEntity, Long> {

    fun findByPublicId(publicId: String): WorkspaceEntity?

    fun findAllByOwnerUserId(ownerUserId: Long): List<WorkspaceEntity>

}
