package com.jobdori.infrastructure.persistence.domain.user.repository

import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.infrastructure.persistence.domain.user.entity.UserIdentityEntity
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager

class UserIdentityCustomRepositoryImpl(
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : UserIdentityCustomRepository {

    override fun existsByProviderAndProviderUserId(
        provider: UserIdentityProvider,
        providerUserId: String,
    ): Boolean {
        val query = jpql {
            selectNew<Long>(
                count(path(UserIdentityEntity::id)),
            ).from(
                entity(UserIdentityEntity::class),
            ).where(
                path(UserIdentityEntity::provider).eq(provider)
                    .and(path(UserIdentityEntity::providerUserId).eq(providerUserId))
            )
        }

        return entityManager.createQuery(query, jpqlRenderContext)
            .setMaxResults(1)
            .singleResult > 0
    }

}
