package com.jobdori.core.application.experiencequestionroom

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomCardResult
import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomResult
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoom
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomSourceType
import com.jobdori.core.domain.experiencequestionroom.repository.ExperienceQuestionRoomApplyRepository
import com.jobdori.core.domain.experiencequestionroom.repository.ExperienceQuestionRoomRepository
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetExperienceQuestionRoomsService(
    private val getJdService: GetJdService,
    private val repository: ExperienceQuestionRoomRepository,
    private val applyRepository: ExperienceQuestionRoomApplyRepository,
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {

    fun get(workspaceId: Long, jdPublicId: String): List<ExperienceQuestionRoomCardResult> {
        val cards = getCards(workspaceId, jdPublicId)
        val appliedIds = applyRepository.findAppliedQuestionRoomIds(cards.map { it.id })
        return cards.map { ExperienceQuestionRoomCardResult(it, it.id in appliedIds) }
    }

    private fun getCards(workspaceId: Long, jdPublicId: String): List<ExperienceQuestionRoomCard> {
        val jd = getJdService.getJd(workspaceId, jdPublicId)
        repository.findByJdId(jd.id)?.let { return it.cards }

        if (jd.responsibilities.isEmpty() && jd.preferredExperiences.isEmpty()) return emptyList()

        val promptType = PromptType.EXPERIENCE_QUESTION_ROOM_GENERATION
        val template = promptTemplateRepository.findByType(promptType)
            ?: throw AiException("프롬프트 없음: $promptType", AiErrorCode.E500_AI_GENERATION_FAILED)
        val result = aiChatClient.generateStructured(
            template.buildStructured(buildUserPrompt(jd.responsibilities, jd.preferredExperiences), ExperienceQuestionRoomResult::class),
        )
        val cards = result.cards
            .filter { it.question.isNotBlank() }
            .distinctBy { it.sourceType to it.index }
            .mapNotNull { card ->
                val sourceText = when (card.sourceType) {
                    ExperienceQuestionRoomSourceType.RESPONSIBILITY -> jd.responsibilities.getOrNull(card.index - 1)
                    ExperienceQuestionRoomSourceType.PREFERRED_EXPERIENCE -> jd.preferredExperiences.getOrNull(card.index - 1)
                } ?: return@mapNotNull null
                ExperienceQuestionRoomCard(UUID.randomUUID().toString(), card.question, card.sourceType, sourceText)
            }
        // 빈 결과를 저장하면 그 JD는 영영 재생성되지 않으므로 저장하지 않고 다음 조회에서 재시도한다.
        if (cards.isEmpty()) return emptyList()
        val room = ExperienceQuestionRoom.newInstance(jd.id, cards)
        return try {
            repository.save(room).cards
        } catch (e: DataIntegrityViolationException) {
            repository.findByJdId(jd.id)?.cards ?: throw e
        }
    }

    private fun buildUserPrompt(responsibilities: List<String>, preferredExperiences: List<String>) = buildString {
        fun section(label: String, items: List<String>) {
            if (items.isEmpty()) return
            appendLine("## $label")
            items.forEachIndexed { index, text -> appendLine("[${index + 1}] $text") }
            appendLine()
        }
        section("담당업무", responsibilities)
        section("우대사항", preferredExperiences)
    }.trim()
}
