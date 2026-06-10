package com.jobdori.infrastructure.persistence.sample.repository

import com.jobdori.infrastructure.persistence.sample.entity.SampleEntity
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class SampleCustomRepository(
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) {

     fun findByName(name: String): SampleEntity? {
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
