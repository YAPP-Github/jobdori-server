package com.jobdori.core.application.experiencerecommendation

import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experiencerecommendation.JdExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.repository.JdExperienceRecommendationRepository
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetExperienceRecommendationService(
    private val jdRepository: JdRepository,
    private val experienceReader: ExperienceReader,
    private val recommendationRepository: JdExperienceRecommendationRepository,
    private val generateService: GenerateExperienceRecommendationService,
) {

    // 경험 세트가 그대로면 캐시 반환, 바뀌었으면(시그니처 불일치) 재생성/갱신.
    @Transactional
    fun getOrRefresh(workspaceId: Long, jdPublicId: String): JdExperienceRecommendation {
        val jd = jdRepository.findByPublicIdAndWorkspaceId(jdPublicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($jdPublicId)입니다")

        val signature = experienceReader.signature(workspaceId)
        recommendationRepository.findByJdId(jd.id)?.let {
            if (it.sourceSignature == signature) return it
        }

        val items = generateService.generate(jd, experienceReader.findAllActive(workspaceId))
        return recommendationRepository.upsert(
            JdExperienceRecommendation.newInstance(jd.id, items, signature),
        )
    }

}
