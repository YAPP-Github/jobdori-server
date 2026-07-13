package com.jobdori.infrastructure.persistence.domain.experience.entity

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Table(name = "experience_project_v1")
@Entity
class ExperienceProjectEntity(
    @Column(nullable = false)
    var workspaceId: Long,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 500)
    var summary: String,

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
    var status: ExperienceProjectStatus,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = ExperienceProject(
        id = id,
        workspaceId = workspaceId,
        name = name,
        summary = summary,
        period = toPeriod(),
        role = role,
        displayOrder = displayOrder,
        status = status,
    )

    private fun toPeriod(): Period? {
        if (startAt == null && endAt == null) {
            return null
        }

        return Period(
            startAt = startAt,
            endAt = endAt,
        )
    }

    companion object {
        fun from(domain: ExperienceProject) = ExperienceProjectEntity(
            workspaceId = domain.workspaceId,
            name = domain.name,
            summary = domain.summary,
            startAt = domain.period?.startAt,
            endAt = domain.period?.endAt,
            role = domain.role,
            displayOrder = domain.displayOrder,
            status = domain.status,
        ).also { it.id = domain.id }
    }

}
