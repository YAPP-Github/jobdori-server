package com.jobdori.core.application.experiencerecommendation

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard
import com.jobdori.core.domain.experiencequestionroom.error.ExperienceQuestionRoomNotFoundException
import com.jobdori.core.domain.experiencequestionroom.repository.ExperienceQuestionRoomRepository
import com.jobdori.core.domain.experiencerecommendation.QuestionRoomExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.RecommendedExperience
import com.jobdori.core.domain.experiencerecommendation.repository.QuestionRoomExperienceRecommendationRepository
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ExperienceRecommendationView(
    val items: List<RecommendedExperience>,
    val experiences: List<Experience>,
)

@Service
class GetExperienceRecommendationService(
    private val jdRepository: JdRepository,
    private val experienceReader: ExperienceReader,
    private val questionRoomRepository: ExperienceQuestionRoomRepository,
    private val recommendationRepository: QuestionRoomExperienceRecommendationRepository,
    private val generateService: GenerateExperienceRecommendationService,
) {

    // 경험 세트가 그대로면 캐시 반환, 바뀌었으면(시그니처 불일치) 재생성/갱신.
    @Transactional
    fun getOrRefresh(workspaceId: Long, jdPublicId: String, questionRoomId: String): ExperienceRecommendationView {
        val jd = jdRepository.findByPublicIdAndWorkspaceId(jdPublicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($jdPublicId)입니다")
        val card = questionRoomRepository.findByJdId(jd.id)?.cards?.firstOrNull { it.id == questionRoomId }
            ?: throw ExperienceQuestionRoomNotFoundException("경험 질문 대화방을 찾지 못했습니다. [questionRoomId=$questionRoomId]")

        val signature = experienceReader.signature(workspaceId)
        val experiences = experienceReader.findAllActive(workspaceId)
        recommendationRepository.findByQuestionRoomId(questionRoomId)?.let {
            if (it.sourceSignature == signature) return ExperienceRecommendationView(it.items, experiences)
        }

        val items = generateService.generate(jd, card, experiences)
        val saved = recommendationRepository.upsert(
            QuestionRoomExperienceRecommendation.newInstance(questionRoomId, items, signature),
        )
        return ExperienceRecommendationView(saved.items, experiences)
    }

}
