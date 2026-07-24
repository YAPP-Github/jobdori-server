package com.jobdori.core.application.experiencerecommendation

import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experiencerecommendation.JdExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.RecommendedExperience
import com.jobdori.core.domain.experiencerecommendation.repository.JdExperienceRecommendationRepository
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 매칭 결과 + 매칭의 기준이 된 JD 지원 전략(jd.strategy, JD 등록 시 생성)을 함께 노출한다.
data class ExperienceRecommendationView(
    val strategy: String,
    val items: List<RecommendedExperience>,
)

@Service
class GetExperienceRecommendationService(
    private val jdRepository: JdRepository,
    private val experienceReader: ExperienceReader,
    private val recommendationRepository: JdExperienceRecommendationRepository,
    private val generateService: GenerateExperienceRecommendationService,
) {

    // 경험 세트가 그대로면 캐시 반환, 바뀌었으면(시그니처 불일치) 재생성/갱신.
    @Transactional
    fun getOrRefresh(workspaceId: Long, jdPublicId: String): ExperienceRecommendationView {
        val jd = jdRepository.findByPublicIdAndWorkspaceId(jdPublicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($jdPublicId)입니다")

        val signature = experienceReader.signature(workspaceId)
        recommendationRepository.findByJdId(jd.id)?.let {
            if (it.sourceSignature == signature) return ExperienceRecommendationView(jd.strategy, it.items)
        }

        val items = generateService.generate(jd, experienceReader.findAllActive(workspaceId))
        val saved = recommendationRepository.upsert(
            JdExperienceRecommendation.newInstance(jd.id, items, signature),
        )
        return ExperienceRecommendationView(jd.strategy, saved.items)
    }

}
