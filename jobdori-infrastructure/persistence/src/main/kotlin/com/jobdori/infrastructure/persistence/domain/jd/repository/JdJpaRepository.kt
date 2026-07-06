package com.jobdori.infrastructure.persistence.domain.jd.repository

import com.jobdori.infrastructure.persistence.domain.jd.entity.JdEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JdJpaRepository : JpaRepository<JdEntity, Long> {
    fun findByPublicIdAndWorkspaceId(publicId: String, workspaceId: Long): JdEntity?
    fun findAllByWorkspaceId(workspaceId: Long): List<JdEntity>
}
