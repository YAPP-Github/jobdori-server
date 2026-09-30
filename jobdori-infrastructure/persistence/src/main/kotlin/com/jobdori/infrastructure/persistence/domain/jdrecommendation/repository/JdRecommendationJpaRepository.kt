package com.jobdori.infrastructure.persistence.domain.jdrecommendation.repository

import com.jobdori.infrastructure.persistence.domain.jdrecommendation.entity.JdRecommendationEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JdRecommendationJpaRepository : JpaRepository<JdRecommendationEntity, Long> {

    @Query(
        """
        select r from JdRecommendationEntity r
        where r.isActive = true
          and (
            :cursorDisplayOrder is null
            or r.displayOrder > :cursorDisplayOrder
            or (r.displayOrder = :cursorDisplayOrder and r.id > :cursorId)
          )
        order by r.displayOrder asc, r.id asc
        """,
    )
    fun findAllActive(
        @Param("cursorDisplayOrder") cursorDisplayOrder: Double?,
        @Param("cursorId") cursorId: Long?,
        pageable: Pageable,
    ): List<JdRecommendationEntity>

    @Query(
        value = """
            select *
            from jd_recommendation_v1
            where is_active = true
              and tags @> cast(:tags as jsonb)
              and (
                cast(:cursorDisplayOrder as double precision) is null
                or display_order > cast(:cursorDisplayOrder as double precision)
                or (
                    display_order = cast(:cursorDisplayOrder as double precision)
                    and id > cast(:cursorId as bigint)
                )
              )
            order by display_order asc, id asc
        """,
        nativeQuery = true,
    )
    fun findAllActiveContainingTags(
        @Param("tags") tags: String,
        @Param("cursorDisplayOrder") cursorDisplayOrder: Double?,
        @Param("cursorId") cursorId: Long?,
        pageable: Pageable,
    ): List<JdRecommendationEntity>
}
