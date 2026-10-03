package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessageRole
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Table(name = "experience_question_room_message_v1")
@Entity
class ExperienceQuestionRoomMessageEntity(
    @Column(nullable = false, length = 36)
    var questionRoomId: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var role: ExperienceQuestionRoomMessageRole,

    @Column(columnDefinition = "text")
    var content: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var experienceIds: List<Long>,

    @Column(columnDefinition = "text")
    var blockTitle: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var bullets: List<String>,

    @Column(columnDefinition = "text")
    var fit: String?,

    @Column(columnDefinition = "text")
    var improvement: String?,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = ExperienceQuestionRoomMessage(
        messageId = id,
        questionRoomId = questionRoomId,
        role = role,
        content = content,
        experienceIds = experienceIds,
        blockTitle = blockTitle,
        bullets = bullets,
        fit = fit,
        improvement = improvement,
        createdAt = createdAt,
    )

    companion object {
        fun from(domain: ExperienceQuestionRoomMessage) = ExperienceQuestionRoomMessageEntity(
            questionRoomId = domain.questionRoomId,
            role = domain.role,
            content = domain.content,
            experienceIds = domain.experienceIds,
            blockTitle = domain.blockTitle,
            bullets = domain.bullets,
            fit = domain.fit,
            improvement = domain.improvement,
        )
    }
}
