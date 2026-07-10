package com.jobdori.infrastructure.persistence.domain.jdinsight.entity

import com.jobdori.core.domain.jdinsight.JdInsight
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "jd_insight_v1")
@Entity
class JdInsightEntity(
    @Column(nullable = false, unique = true, updatable = false)
    var jdId: Long,

    @Column(nullable = false, columnDefinition = "text")
    var keyPoints: String,

    @Column(nullable = false, columnDefinition = "text")
    var strategy: String,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = JdInsight(
        id = id,
        jdId = jdId,
        keyPoints = keyPoints,
        strategy = strategy,
        createdAt = createdAt,
    )

    companion object {
        fun from(insight: JdInsight) = JdInsightEntity(
            jdId = insight.jdId,
            keyPoints = insight.keyPoints,
            strategy = insight.strategy,
        ).also { it.id = insight.id }
    }

}
