package com.jobdori.core.domain.experiencequestionroom

import java.time.LocalDateTime

enum class ExperienceQuestionRoomMessageRole { USER, AI }

data class ExperienceQuestionRoomMessage(
    val messageId: Long,
    val questionRoomId: String,
    val role: ExperienceQuestionRoomMessageRole,
    val content: String?,
    val experienceIds: List<Long>,
    val blockTitle: String?,
    val bullets: List<String>,
    val fit: String?,
    val improvement: String?,
    val createdAt: LocalDateTime? = null,
) {

    companion object {
        fun user(questionRoomId: String, content: String?, experienceIds: List<Long>) = ExperienceQuestionRoomMessage(
            messageId = 0L,
            questionRoomId = questionRoomId,
            role = ExperienceQuestionRoomMessageRole.USER,
            content = content,
            experienceIds = experienceIds,
            blockTitle = null,
            bullets = emptyList(),
            fit = null,
            improvement = null,
        )

        fun ai(
            questionRoomId: String,
            blockTitle: String,
            bullets: List<String>,
            fit: String,
            improvement: String,
        ) = ExperienceQuestionRoomMessage(
            messageId = 0L,
            questionRoomId = questionRoomId,
            role = ExperienceQuestionRoomMessageRole.AI,
            content = null,
            experienceIds = emptyList(),
            blockTitle = blockTitle,
            bullets = bullets,
            fit = fit,
            improvement = improvement,
        )
    }
}

data class ExperienceQuestionRoomApply(
    val questionRoomId: String,
    val resumeId: Long,
    val itemId: Long,
)
