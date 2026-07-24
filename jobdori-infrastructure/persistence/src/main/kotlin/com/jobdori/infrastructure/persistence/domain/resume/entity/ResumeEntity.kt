package com.jobdori.infrastructure.persistence.domain.resume.entity

import com.jobdori.core.domain.resume.CoreCompetencyGenerationStatus
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "resume_v1")
@Entity
class ResumeEntity(
    @Column(nullable = false)
    var workspaceId: Long,

    @Column
    var targetJdId: Long?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var template: ResumeTemplate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ResumeStatus,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var coreCompetencyGenerationStatus: CoreCompetencyGenerationStatus =
        CoreCompetencyGenerationStatus.NOT_GENERATED,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = Resume(
        id = id,
        workspaceId = workspaceId,
        targetJdId = targetJdId,
        template = template,
        status = status,
        coreCompetencyGenerationStatus = coreCompetencyGenerationStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun from(domain: Resume) = ResumeEntity(
            workspaceId = domain.workspaceId,
            targetJdId = domain.targetJdId,
            template = domain.template,
            status = domain.status,
            coreCompetencyGenerationStatus = domain.coreCompetencyGenerationStatus,
        ).also { it.id = domain.id }
    }

}
