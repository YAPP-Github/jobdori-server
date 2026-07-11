package com.jobdori.infrastructure.persistence.domain.experiencerecommendation.entity

import com.jobdori.core.domain.experiencerecommendation.JdExperienceRecommendation
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

@Table(name = "jd_experience_recommendation_v1")
@Entity
class JdExperienceRecommendationEntity(
    @Column(nullable = false, unique = true, updatable = false)
    var jdId: Long,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var items: List<RecommendedExperience>,

    @Column(nullable = false, columnDefinition = "text")
    var sourceSignature: String,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = JdExperienceRecommendation(
        id = id,
        jdId = jdId,
        items = items,
        sourceSignature = sourceSignature,
        createdAt = createdAt,
    )

    companion object {
        fun from(domain: JdExperienceRecommendation) = JdExperienceRecommendationEntity(
            jdId = domain.jdId,
            items = domain.items,
            sourceSignature = domain.sourceSignature,
        ).also { it.id = domain.id }
    }

}
