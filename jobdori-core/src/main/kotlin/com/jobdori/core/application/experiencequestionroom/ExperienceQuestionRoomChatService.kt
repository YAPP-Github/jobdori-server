package com.jobdori.core.application.experiencequestionroom

import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomChatResult
import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomDetail
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperiencePromptText
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessageRole
import com.jobdori.core.domain.experiencequestionroom.error.ExperienceQuestionRoomNotFoundException
import com.jobdori.core.domain.experiencequestionroom.repository.ExperienceQuestionRoomMessageRepository
import com.jobdori.core.domain.experiencequestionroom.repository.ExperienceQuestionRoomRepository
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdPromptText
import com.jobdori.core.domain.jd.JdSortType
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service

@Service
class ExperienceQuestionRoomChatService(
    private val getJdService: GetJdService,
    private val roomRepository: ExperienceQuestionRoomRepository,
    private val messageRepository: ExperienceQuestionRoomMessageRepository,
    private val experienceReader: ExperienceReader,
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {

    fun getDetail(workspaceId: Long, questionRoomId: String): ExperienceQuestionRoomDetail {
        val (_, room) = findRoomWithJd(workspaceId, questionRoomId)
        return ExperienceQuestionRoomDetail(
            room,
            messageRepository.findAllByQuestionRoomId(questionRoomId),
        )
    }

    // AI 호출을 기다리는 동안 DB 커넥션을 잡지 않도록 트랜잭션을 걸지 않는다. 메시지 쌍 저장만 자기 트랜잭션을 쓴다.
    fun send(
        workspaceId: Long,
        questionRoomId: String,
        content: String?,
        experienceIds: List<Long>,
    ): ExperienceQuestionRoomMessage {
        val (jd, room) = findRoomWithJd(workspaceId, questionRoomId)
        val userContent = content?.takeIf { it.isNotBlank() }
        val selectedIds = experienceIds.distinct()
        if (userContent == null && selectedIds.isEmpty()) {
            throw invalidArgument("content", "내용을 입력하거나 경험을 선택해 주세요.")
        }
        val selected = selectedIds.map { experienceReader.getExperience(workspaceId, it) }
        if (selected.map { it.projectId }.distinct().size > 1) {
            throw invalidArgument("experienceIds", "같은 프로젝트의 경험만 함께 선택할 수 있어요.")
        }

        val history = messageRepository.findAllByQuestionRoomId(questionRoomId)
        val experiences = experiencesInRoom(workspaceId, history, selected)
        val template = promptTemplateRepository.findByType(PromptType.EXPERIENCE_QUESTION_ROOM_CHAT)
            ?: throw AiException(
                "프롬프트 없음: ${PromptType.EXPERIENCE_QUESTION_ROOM_CHAT}",
                AiErrorCode.E500_AI_GENERATION_FAILED,
            )
        val generated = aiChatClient.generateStructured(
            template.buildStructured(
                buildUserPrompt(jd, room, experiences, history, userContent, selected),
                ExperienceQuestionRoomChatResult::class,
            ),
        )

        val user = ExperienceQuestionRoomMessage.user(questionRoomId, userContent, selectedIds)
        val ai = ExperienceQuestionRoomMessage.ai(
            questionRoomId = questionRoomId,
            blockTitle = generated.block.title,
            bullets = generated.block.bullets,
            fit = generated.feedback.fit,
            improvement = generated.feedback.improvement,
        )
        return messageRepository.savePair(user, ai).last()
    }

    // 이전 턴에서 고른 경험도 후속 요청(예: 더 짧게)에 쓰이도록 대화방에서 고른 경험 전체를 넣는다. 삭제된 경험은 건너뛴다.
    private fun experiencesInRoom(
        workspaceId: Long,
        history: List<ExperienceQuestionRoomMessage>,
        selected: List<Experience>,
    ): List<Experience> {
        val previousIds = history.flatMap { it.experienceIds }.toSet() - selected.map { it.id }.toSet()
        if (previousIds.isEmpty()) return selected
        return selected + experienceReader.findAllActive(workspaceId).filter { it.id in previousIds }
    }

    private fun buildUserPrompt(
        jd: Jd,
        room: ExperienceQuestionRoomCard,
        experiences: List<Experience>,
        history: List<ExperienceQuestionRoomMessage>,
        userContent: String?,
        selected: List<Experience>,
    ): String {
        val titleById = experiences.associate { it.id to it.title }
        return buildString {
            appendLine("## JD")
            appendLine(JdPromptText.of(jd))
            appendLine()
            appendLine("## 질문 카드")
            appendLine("질문: ${room.question}")
            appendLine("출처 유형: ${room.sourceType}")
            appendLine("출처 내용: ${room.sourceText}")
            appendLine()
            if (experiences.isNotEmpty()) {
                appendLine("## 대화방에서 선택한 경험")
                experiences.forEach { experience ->
                    appendLine("[${experience.title}]")
                    appendLine(ExperiencePromptText.contentsOf(experience))
                    appendLine()
                }
            }
            if (history.isNotEmpty()) {
                appendLine("## 이전 대화")
                history.forEach { appendLine(renderHistory(it, titleById)) }
                appendLine()
            }
            appendLine("## 이번 사용자 입력")
            userContent?.let { appendLine(it) }
            if (selected.isNotEmpty()) appendLine("선택한 경험: ${selected.joinToString(", ") { it.title }}")
        }.trim()
    }

    private fun renderHistory(message: ExperienceQuestionRoomMessage, titleById: Map<Long, String>): String =
        when (message.role) {
            ExperienceQuestionRoomMessageRole.USER -> buildString {
                append("사용자: ${message.content.orEmpty()}")
                val titles = message.experienceIds.mapNotNull { titleById[it] }
                if (titles.isNotEmpty()) append(" (선택한 경험: ${titles.joinToString(", ")})")
            }
            ExperienceQuestionRoomMessageRole.AI ->
                "AI 블록: ${message.blockTitle}\n${message.bullets.joinToString("\n") { "- $it" }}\n피드백: ${message.fit} ${message.improvement}"
        }

    private fun findRoomWithJd(workspaceId: Long, questionRoomId: String): Pair<Jd, ExperienceQuestionRoomCard> {
        val jds = getJdService.getJds(workspaceId, JdSortType.LATEST)
        val roomsByJdId = roomRepository.findAllByJdIdIn(jds.map { it.id }).associateBy { it.jdId }
        jds.forEach { jd ->
            roomsByJdId[jd.id]?.cards?.firstOrNull { it.id == questionRoomId }?.let { return jd to it }
        }
        throw ExperienceQuestionRoomNotFoundException("경험 질문 대화방을 찾지 못했습니다. [questionRoomId=$questionRoomId]")
    }

    private fun invalidArgument(field: String, reason: String) =
        InvalidArgumentsException(message = reason, details = listOf(ErrorDetail(field = field, reason = reason)))
}
