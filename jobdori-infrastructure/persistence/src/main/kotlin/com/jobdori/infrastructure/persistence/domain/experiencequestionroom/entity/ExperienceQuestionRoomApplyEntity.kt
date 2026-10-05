package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomApply
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Table(
    name = "experience_question_room_apply_v1",
    uniqueConstraints = [UniqueConstraint(columnNames = ["question_room_id", "resume_id"])],
)
@Entity
class ExperienceQuestionRoomApplyEntity(
    @Column(nullable = false, length = 36)
    var questionRoomId: String,

    @Column(nullable = false)
    var resumeId: Long,

    @Column(nullable = false)
    var itemId: Long,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = ExperienceQuestionRoomApply(questionRoomId = questionRoomId, resumeId = resumeId, itemId = itemId)

    companion object {
        fun from(domain: ExperienceQuestionRoomApply) = ExperienceQuestionRoomApplyEntity(
            questionRoomId = domain.questionRoomId,
            resumeId = domain.resumeId,
            itemId = domain.itemId,
        )
    }
}
