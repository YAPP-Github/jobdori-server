package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoom
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Table(name = "jd_experience_question_room_v1")
@Entity
class ExperienceQuestionRoomEntity(
    @Column(nullable = false, unique = true, updatable = false)
    var jdId: Long,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var cards: List<ExperienceQuestionRoomCard>,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = ExperienceQuestionRoom(id = id, jdId = jdId, cards = cards)

    companion object {
        fun from(domain: ExperienceQuestionRoom) = ExperienceQuestionRoomEntity(
            jdId = domain.jdId,
            cards = domain.cards,
        )
    }
}
