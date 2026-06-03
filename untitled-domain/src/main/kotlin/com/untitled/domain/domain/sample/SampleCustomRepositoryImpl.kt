package com.untitled.domain.domain.sample

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class SampleCustomRepositoryImpl(
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : SampleCustomRepository {

    override fun findByName(name: String): SampleEntity? {
        val query = jpql {
            select(entity(SampleEntity::class))
                .from(entity(SampleEntity::class))
                .whereAnd(path(SampleEntity::name).eq(name))
        }

        return entityManager.createQuery(query, jpqlRenderContext)
            .setMaxResults(1)
            .resultList
            .firstOrNull()
    }

}
