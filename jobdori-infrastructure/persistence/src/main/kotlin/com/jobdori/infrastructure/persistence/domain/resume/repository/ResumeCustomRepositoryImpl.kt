package com.jobdori.infrastructure.persistence.domain.resume.repository

import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeEntity
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager

class ResumeCustomRepositoryImpl(
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : ResumeCustomRepository {

    override fun countByWorkspaceIdAndStatusIn(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
    ): Map<ResumeStatus, Long> {
        if (statuses.isEmpty()) {
            return emptyMap()
        }

        val query = jpql {
            selectNew<ResumeStatusCount>(
                path(ResumeEntity::status),
                count(path(ResumeEntity::id)),
            ).from(
                entity(ResumeEntity::class),
            ).where(
                path(ResumeEntity::workspaceId).eq(workspaceId)
                    .and(path(ResumeEntity::status).`in`(statuses)),
            ).groupBy(
                path(ResumeEntity::status),
            )
        }

        return entityManager.createQuery(query, jpqlRenderContext).resultList
            .associate { it.status to it.count }
    }

    data class ResumeStatusCount(
        val status: ResumeStatus,
        val count: Long,
    )

}
