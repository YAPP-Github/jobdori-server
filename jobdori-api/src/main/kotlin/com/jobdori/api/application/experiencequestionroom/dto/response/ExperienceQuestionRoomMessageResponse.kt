package com.jobdori.api.application.experiencequestionroom.dto.response

import com.jobdori.common.time.toInstantAtSystemDefaultZone
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessageRole
import java.time.Instant

data class ExperienceQuestionRoomMessageResponse(
    val messageId: Long,
    val role: ExperienceQuestionRoomMessageRole,
    val content: String?,
    val experienceIds: List<Long>?,
    val block: Block?,
    val feedback: Feedback?,
    val createdAt: Instant,
) {

    data class Block(val title: String, val bullets: List<String>)

    data class Feedback(val fit: String, val improvement: String)

    companion object {
        fun from(message: ExperienceQuestionRoomMessage): ExperienceQuestionRoomMessageResponse {
            val isAi = message.role == ExperienceQuestionRoomMessageRole.AI
            return ExperienceQuestionRoomMessageResponse(
                messageId = message.messageId,
                role = message.role,
                content = message.content,
                experienceIds = message.experienceIds.takeUnless { isAi },
                block = if (isAi) Block(message.blockTitle.orEmpty(), message.bullets) else null,
                feedback = if (isAi) Feedback(message.fit.orEmpty(), message.improvement.orEmpty()) else null,
                createdAt = requireNotNull(message.createdAt) { "저장되지 않은 메시지입니다." }.toInstantAtSystemDefaultZone(),
            )
        }
    }
}
