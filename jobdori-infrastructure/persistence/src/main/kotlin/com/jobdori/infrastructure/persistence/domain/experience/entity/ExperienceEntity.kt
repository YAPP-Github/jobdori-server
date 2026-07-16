package com.jobdori.infrastructure.persistence.domain.experience.entity

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate

@Table(name = "experience_v1")
@Entity
class ExperienceEntity(
    @Column(nullable = false)
    var workspaceId: Long,

    @Column(nullable = false)
    var projectId: Long,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var tags: List<String>,

    @Column(nullable = false, length = 150)
    var title: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var contents: ExperienceContents,

    @Column
    var startAt: LocalDate?,

    @Column
    var endAt: LocalDate?,

    @Column(length = 100)
    var role: String?,

    @Column(nullable = false)
    var displayOrder: Double,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ExperienceStatus,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = Experience(
        id = id,
        workspaceId = workspaceId,
        projectId = projectId,
        tags = tags,
        title = title,
        contents = contents,
        period = toPeriod(),
        role = role,
        displayOrder = displayOrder,
        status = status,
    )

    private fun toPeriod(): Period? {
        if (startAt == null && endAt == null) return null
        return Period(startAt = startAt, endAt = endAt)
    }

    companion object {
        fun from(domain: Experience) = ExperienceEntity(
            workspaceId = domain.workspaceId,
            projectId = domain.projectId,
            tags = domain.tags,
            title = domain.title,
            contents = domain.contents,
            startAt = domain.period?.startAt,
            endAt = domain.period?.endAt,
            role = domain.role,
            displayOrder = domain.displayOrder,
            status = domain.status,
        ).also { it.id = domain.id }
    }

}
