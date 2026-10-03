package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.repository

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomApply
import com.jobdori.core.domain.experiencequestionroom.repository.ExperienceQuestionRoomApplyRepository
import com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity.ExperienceQuestionRoomApplyEntity
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ExperienceQuestionRoomApplyRepositoryImpl(
    private val jpa: ExperienceQuestionRoomApplyJpaRepository,
    private val entityManager: EntityManager,
) : ExperienceQuestionRoomApplyRepository {

    @Transactional
    override fun lockResume(resumeId: Long, workspaceId: Long): Boolean =
        entityManager.createNativeQuery("select id from resume_v1 where id = :id and workspace_id = :workspaceId for update")
            .setParameter("id", resumeId)
            .setParameter("workspaceId", workspaceId)
            .resultList
            .isNotEmpty()

    @Transactional(readOnly = true)
    override fun findByQuestionRoomIdAndResumeId(questionRoomId: String, resumeId: Long): ExperienceQuestionRoomApply? =
        jpa.findByQuestionRoomIdAndResumeId(questionRoomId, resumeId)?.toDomain()

    @Transactional
    override fun save(apply: ExperienceQuestionRoomApply): ExperienceQuestionRoomApply {
        val entity = jpa.findByQuestionRoomIdAndResumeId(apply.questionRoomId, apply.resumeId)
            ?.apply { itemId = apply.itemId }
            ?: ExperienceQuestionRoomApplyEntity.from(apply)
        return jpa.save(entity).toDomain()
    }
}
