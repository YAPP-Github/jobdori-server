package com.jobdori.infrastructure.persistence.user.repository

import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.infrastructure.persistence.user.entity.UserIdentifyEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserIdentifyJpaRepository : JpaRepository<UserIdentifyEntity, Long> {

    fun findByIdentifyProviderAndIdentifyId(
        identifyProvider: UserIdentifyProvider,
        identifyId: String,
    ): UserIdentifyEntity?

}
