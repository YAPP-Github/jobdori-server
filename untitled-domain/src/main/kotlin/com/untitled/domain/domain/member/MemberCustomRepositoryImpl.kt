package com.untitled.domain.domain.member

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class MemberCustomRepositoryImpl(
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : MemberCustomRepository {

    override fun findByName(name: String): MemberEntity? {
        val query = jpql {
            select(entity(MemberEntity::class))
                .from(entity(MemberEntity::class))
                .whereAnd(path(MemberEntity::name).eq(name))
        }

        return entityManager.createQuery(query, jpqlRenderContext)
            .setMaxResults(1)
            .resultList
            .firstOrNull()
    }

}
