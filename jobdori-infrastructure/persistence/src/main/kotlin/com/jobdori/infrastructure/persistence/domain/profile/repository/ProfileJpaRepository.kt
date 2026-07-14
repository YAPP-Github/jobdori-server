package com.jobdori.infrastructure.persistence.domain.profile.repository

import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProfileJpaRepository : JpaRepository<ProfileEntity, Long> {

    fun findByWorkspaceId(workspaceId: Long): ProfileEntity?

}
