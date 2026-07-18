package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceEntity
import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.extension.createQuery
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Pageable

class ExperienceCustomRepositoryImpl(
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : ExperienceCustomRepository {

    override fun updateStatusByWorkspaceIdAndProjectId(
        workspaceId: Long,
        projectId: Long,
        status: ExperienceStatus,
    ) {
        val query = jpql {
            update(
                entity(ExperienceEntity::class),
            ).set(
                path(ExperienceEntity::status),
                status,
            ).where(
                path(ExperienceEntity::workspaceId).eq(workspaceId)
                    .and(path(ExperienceEntity::projectId).eq(projectId)),
            )
        }

        entityManager.flush()
        entityManager.createQuery(query, jpqlRenderContext).executeUpdate()
        entityManager.clear()
    }

    override fun searchAllByWorkspaceIdAndStatus(
        workspaceId: Long,
        status: ExperienceStatus,
        keywordPattern: String,
        cursorId: Long?,
        pageable: Pageable,
    ): List<ExperienceEntity> {
        val cursorCondition = cursorId?.let { "and id < :cursorId" }.orEmpty()
        val query = entityManager.createNativeQuery(
            """
            select *
            from experience_v1
            where workspace_id = :workspaceId
              and status = :status
              and (
                lower(title) like :keywordPattern escape '\'
                or lower(cast(contents as varchar)) like :keywordPattern escape '\'
              )
              $cursorCondition
            order by id desc
            """.trimIndent(),
            ExperienceEntity::class.java,
        )
            .setParameter("workspaceId", workspaceId)
            .setParameter("status", status.name)
            .setParameter("keywordPattern", keywordPattern)
            .setMaxResults(pageable.pageSize)

        if (cursorId != null) {
            query.setParameter("cursorId", cursorId)
        }

        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<ExperienceEntity>
    }

    override fun countByWorkspaceIdAndProjectIdsAndStatus(
        workspaceId: Long,
        projectIds: Collection<Long>,
        status: ExperienceStatus,
    ): Map<Long, Long> {
        if (projectIds.isEmpty()) {
            return emptyMap()
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
            .associate { it.projectId to it.experienceCount }
    }

    data class ExperienceProjectCount(
        val projectId: Long,
        val experienceCount: Long,
    )

}
