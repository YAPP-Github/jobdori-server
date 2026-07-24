package com.jobdori.core.application.experiencerecommendation

import com.jobdori.core.application.ai.jd.ExtractJdStrategyService
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experiencerecommendation.JdExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.RecommendedExperience
import com.jobdori.core.domain.experiencerecommendation.repository.JdExperienceRecommendationRepository
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import com.jobdori.core.domain.profile.service.ProfileReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 매칭 결과 + 매칭의 기준이 된 JD 지원 전략(jd.strategy, 경험 선택 시점에 지연 생성)을 함께 노출한다.
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
    private val extractJdStrategyService: ExtractJdStrategyService,
    private val profileReader: ProfileReader,
) {

    // 경험 세트가 그대로면 캐시 반환, 바뀌었으면(시그니처 불일치) 재생성/갱신.
    @Transactional
    fun getOrRefresh(workspaceId: Long, jdPublicId: String): ExperienceRecommendationView {
        val jd = ensureStrategy(
            jdRepository.findByPublicIdAndWorkspaceId(jdPublicId, workspaceId)
                ?: throw JdNotFoundException("등록되지 않은 JD($jdPublicId)입니다"),
        )

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

    // strategy는 JD 등록 임계경로가 아니라 경험 선택 시점에 지연 생성한다(등록 지연/실패를 전략 생성과 분리).
    // 생성 실패 시 빈 값을 유지해 등록/추천은 계속 동작하고, 다음 접근 때 재시도한다.
    private fun ensureStrategy(jd: Jd): Jd {
        if (jd.strategy.isNotBlank()) return jd
        val profile = profileReader.getDetail(profileReader.getOrCreateProfile(jd.workspaceId))
        val strategy = runCatching { extractJdStrategyService.generate(jd, profile) }.getOrNull()
        return if (strategy.isNullOrBlank()) jd else jdRepository.save(jd.copy(strategy = strategy))
    }

}
