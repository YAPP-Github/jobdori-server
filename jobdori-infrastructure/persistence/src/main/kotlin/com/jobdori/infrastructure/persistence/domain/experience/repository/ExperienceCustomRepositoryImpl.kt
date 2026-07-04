package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceEntity
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceProjectCount
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Pageable

class ExperienceCustomRepositoryImpl(
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : ExperienceCustomRepository {

    override fun searchAllByWorkspaceIdAndStatus(
        workspaceId: Long,
        status: ExperienceStatus,
        keywordPattern: String,
        cursorId: Long?,
        pageable: Pageable,
    ): List<ExperienceEntity> {
        val query = jpql {
            select(
                entity(ExperienceEntity::class),
            ).from(
                entity(ExperienceEntity::class),
            ).where(
                if (cursorId == null) {
                    and(
                        path(ExperienceEntity::workspaceId).eq(workspaceId),
                        path(ExperienceEntity::status).eq(status),
                        or(
                            lower(path(ExperienceEntity::title)).like(keywordPattern, '\\'),
                            lower(path(ExperienceEntity::contents)).like(keywordPattern, '\\'),
                        ),
                    )
                } else {
                    and(
                        path(ExperienceEntity::workspaceId).eq(workspaceId),
                        path(ExperienceEntity::status).eq(status),
                        or(
                            lower(path(ExperienceEntity::title)).like(keywordPattern, '\\'),
                            lower(path(ExperienceEntity::contents)).like(keywordPattern, '\\'),
                        ),
                        path(ExperienceEntity::id).lessThan(cursorId),
                    )
                },
            ).orderBy(
                path(ExperienceEntity::id).desc(),
            )
        }

        return entityManager.createQuery(query, jpqlRenderContext)
            .setMaxResults(pageable.pageSize)
            .resultList
    }

    override fun countByWorkspaceIdAndProjectIdsAndStatus(
        workspaceId: Long,
        projectIds: Collection<Long>,
        status: ExperienceStatus,
    ): List<ExperienceProjectCount> {
        if (projectIds.isEmpty()) {
            return emptyList()
        }

        val query = jpql {
            selectNew<ExperienceProjectCount>(
                path(ExperienceEntity::projectId),
                count(path(ExperienceEntity::id)),
            ).from(
                entity(ExperienceEntity::class),
            ).where(
                path(ExperienceEntity::workspaceId).eq(workspaceId)
                    .and(path(ExperienceEntity::projectId).`in`(projectIds))
                    .and(path(ExperienceEntity::status).eq(status)),
            ).groupBy(
                path(ExperienceEntity::projectId),
            )
        }

        return entityManager.createQuery(query, jpqlRenderContext).resultList
    }

}
