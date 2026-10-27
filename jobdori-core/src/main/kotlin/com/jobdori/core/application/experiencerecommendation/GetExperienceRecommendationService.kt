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
import com.jobdori.core.support.concurrent.SingleFlight
import org.springframework.stereotype.Service

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
    private val singleFlight: SingleFlight,
) {

    // 경험 세트가 그대로면 캐시 반환, 바뀌었으면(시그니처 불일치) 재생성/갱신.
    // AI 호출을 기다리는 동안 DB 커넥션을 잡지 않도록 트랜잭션을 걸지 않는다. 조회와 upsert는 각자 트랜잭션을 쓴다.
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

        val items = singleFlight.execute("recommendation:$questionRoomId:$signature") {
            val generated = generateService.generate(jd, card, experiences)
            // 생성 중 경험이 바뀌었으면 오래된 결과로 최신을 덮지 않는다. 다음 조회가 새 시그니처로 재생성한다.
            if (experienceReader.signature(workspaceId) != signature) return@execute generated
            recommendationRepository.upsert(
                QuestionRoomExperienceRecommendation.newInstance(questionRoomId, generated, signature),
            ).items
        }
        return ExperienceRecommendationView(items, experiences)
    }

}
