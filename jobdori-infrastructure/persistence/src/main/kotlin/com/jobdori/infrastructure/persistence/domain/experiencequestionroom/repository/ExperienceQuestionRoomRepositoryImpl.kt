package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.repository

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoom
import com.jobdori.core.domain.experiencequestionroom.repository.ExperienceQuestionRoomRepository
import com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity.ExperienceQuestionRoomEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ExperienceQuestionRoomRepositoryImpl(
    private val jpa: ExperienceQuestionRoomJpaRepository,
) : ExperienceQuestionRoomRepository {

    @Transactional(readOnly = true)
    override fun findByJdId(jdId: Long): ExperienceQuestionRoom? = jpa.findByJdId(jdId)?.toDomain()

    @Transactional(readOnly = true)
    override fun findAllByJdIdIn(jdIds: Collection<Long>): List<ExperienceQuestionRoom> =
        if (jdIds.isEmpty()) emptyList() else jpa.findAllByJdIdIn(jdIds).map { it.toDomain() }

    @Transactional
    override fun save(room: ExperienceQuestionRoom): ExperienceQuestionRoom =
        jpa.save(ExperienceQuestionRoomEntity.from(room)).toDomain()
}
