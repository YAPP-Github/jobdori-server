package com.jobdori.infrastructure.persistence.domain.jdrecommendation.entity

import com.jobdori.core.domain.jdrecommendation.JdRecommendation
import com.jobdori.core.domain.jdrecommendation.JdRecommendationTag
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Table(name = "jd_recommendation_v1")
@Entity
class JdRecommendationEntity(
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var tags: List<JdRecommendationTag>?,

    @Column(nullable = false)
    var companyName: String,

    @Column(nullable = false)
    var title: String,

    var recruitmentStartAt: LocalDateTime?,

    var recruitmentEndAt: LocalDateTime?,

    @Column(nullable = false, columnDefinition = "text")
    var sourceUrl: String,

    @Column(nullable = false)
    var displayOrder: Double,

    @Column(nullable = false)
    var isActive: Boolean,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = JdRecommendation(
        id = id,
        tags = tags,
        companyName = companyName,
        title = title,
        recruitmentStartAt = recruitmentStartAt,
        recruitmentEndAt = recruitmentEndAt,
        sourceUrl = sourceUrl,
        displayOrder = displayOrder,
        isActive = isActive,
        createdAt = createdAt,
    )
}
