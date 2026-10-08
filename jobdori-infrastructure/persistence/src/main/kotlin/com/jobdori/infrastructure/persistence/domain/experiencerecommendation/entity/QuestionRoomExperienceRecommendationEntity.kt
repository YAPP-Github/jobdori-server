package com.jobdori.infrastructure.persistence.domain.experiencerecommendation.entity

import com.jobdori.core.domain.experiencerecommendation.QuestionRoomExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.RecommendedExperience
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Table(name = "question_room_experience_recommendation_v1")
@Entity
class QuestionRoomExperienceRecommendationEntity(
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    var questionRoomId: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var items: List<RecommendedExperience>,

    @Column(nullable = false, columnDefinition = "text")
    var sourceSignature: String,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = QuestionRoomExperienceRecommendation(
        id = id,
        questionRoomId = questionRoomId,
        items = items,
        sourceSignature = sourceSignature,
        createdAt = createdAt,
    )

    companion object {
        fun from(domain: QuestionRoomExperienceRecommendation) = QuestionRoomExperienceRecommendationEntity(
            questionRoomId = domain.questionRoomId,
            items = domain.items,
            sourceSignature = domain.sourceSignature,
        ).also { it.id = domain.id }
    }

}
