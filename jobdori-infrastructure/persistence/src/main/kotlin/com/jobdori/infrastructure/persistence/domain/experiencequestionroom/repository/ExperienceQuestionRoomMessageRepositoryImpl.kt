package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.repository

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage
import com.jobdori.core.domain.experiencequestionroom.repository.ExperienceQuestionRoomMessageRepository
import com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity.ExperienceQuestionRoomMessageEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ExperienceQuestionRoomMessageRepositoryImpl(
    private val jpa: ExperienceQuestionRoomMessageJpaRepository,
) : ExperienceQuestionRoomMessageRepository {

    @Transactional(readOnly = true)
    override fun findAllByQuestionRoomId(questionRoomId: String): List<ExperienceQuestionRoomMessage> =
        jpa.findAllByQuestionRoomIdOrderByCreatedAtAscIdAsc(questionRoomId).map { it.toDomain() }

    @Transactional
    override fun savePair(user: ExperienceQuestionRoomMessage, ai: ExperienceQuestionRoomMessage): List<ExperienceQuestionRoomMessage> =
        jpa.saveAll(listOf(user, ai).map(ExperienceQuestionRoomMessageEntity::from)).map { it.toDomain() }
}
