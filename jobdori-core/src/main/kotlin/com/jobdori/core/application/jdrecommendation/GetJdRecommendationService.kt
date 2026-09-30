package com.jobdori.core.application.jdrecommendation

import com.jobdori.common.model.SliceResult
import com.jobdori.core.domain.jdrecommendation.JdRecommendation
import com.jobdori.core.domain.jdrecommendation.JdRecommendationTag
import com.jobdori.core.domain.jdrecommendation.repository.JdRecommendationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetJdRecommendationService(
    private val jdRecommendationRepository: JdRecommendationRepository,
) {

    @Transactional(readOnly = true)
    fun getRecommendations(
        tags: Collection<JdRecommendationTag>,
        cursor: String?,
        size: Int,
    ): SliceResult<JdRecommendation> {
        val parsedCursor = cursor?.let(::parseCursor)
        val recommendations = if (tags.isEmpty()) {
            jdRecommendationRepository.findAllActive(
                cursorDisplayOrder = parsedCursor?.first,
                cursorId = parsedCursor?.second,
                size = size + 1,
            )
        } else {
            jdRecommendationRepository.findAllActiveContainingTags(
                tags = tags.distinct(),
                cursorDisplayOrder = parsedCursor?.first,
                cursorId = parsedCursor?.second,
                size = size + 1,
            )
        }
        val page = recommendations.take(size)

        return SliceResult(
            items = page,
            nextCursor = if (recommendations.size > size) {
                page.lastOrNull()?.let { "${it.displayOrder}:${it.id}" }
            } else {
                null
            },
        )
    }

    private fun parseCursor(cursor: String): Pair<Double, Long> {
        val parts = cursor.split(":")
        require(parts.size == 2) { "잘못된 추천 공고 커서입니다." }
        val displayOrder = parts[0].toDoubleOrNull()
        val id = parts[1].toLongOrNull()
        require(displayOrder != null && id != null && id > 0) { "잘못된 추천 공고 커서입니다." }
        return displayOrder to id
    }
}
