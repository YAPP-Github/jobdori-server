package com.jobdori.infrastructure.persistence.domain.jdrecommendation.repository

import com.jobdori.core.domain.jdrecommendation.JdRecommendation
import com.jobdori.core.domain.jdrecommendation.JdRecommendationTag
import com.jobdori.core.domain.jdrecommendation.repository.JdRecommendationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JdRecommendationRepositoryImpl(
    private val jpa: JdRecommendationJpaRepository,
) : JdRecommendationRepository {

    @Transactional(readOnly = true)
    override fun findAllActive(
        cursorDisplayOrder: Double?,
        cursorId: Long?,
        size: Int,
    ): List<JdRecommendation> = jpa.findAllActive(
        cursorDisplayOrder = cursorDisplayOrder,
        cursorId = cursorId,
        pageable = PageRequest.of(0, size),
    ).map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findAllActiveContainingTags(
        tags: Collection<JdRecommendationTag>,
        cursorDisplayOrder: Double?,
        cursorId: Long?,
        size: Int,
    ): List<JdRecommendation> = jpa.findAllActiveContainingTags(
        tags = tagsJson(tags),
        cursorDisplayOrder = cursorDisplayOrder,
        cursorId = cursorId,
        pageable = PageRequest.of(0, size),
    ).map { it.toDomain() }

    private fun tagsJson(tags: Collection<JdRecommendationTag>): String =
        tags.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]") { it.name }
}
