package com.jobdori.infrastructure.persistence.domain.user.repository

import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.infrastructure.persistence.domain.user.entity.UserIdentityEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserIdentityJpaRepository : JpaRepository<UserIdentityEntity, Long>, UserIdentityCustomRepository {

    fun findByProviderAndProviderUserId(
        provider: UserIdentityProvider,
        providerUserId: String,
    ): UserIdentityEntity?

}
