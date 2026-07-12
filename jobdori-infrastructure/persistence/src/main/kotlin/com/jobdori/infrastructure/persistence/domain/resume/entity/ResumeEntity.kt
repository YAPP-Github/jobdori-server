package com.jobdori.infrastructure.persistence.domain.resume.entity

import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "resume_v1")
@Entity
class ResumeEntity(
    @Column(nullable = false)
    var workspaceId: Long,

    @Column
    var targetJdId: Long?,

    @Column(nullable = false, length = 100)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var template: ResumeTemplate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ResumeStatus,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = Resume(
        id = id,
        workspaceId = workspaceId,
        targetJdId = targetJdId,
        title = title,
        template = template,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun from(domain: Resume) = ResumeEntity(
            workspaceId = domain.workspaceId,
            targetJdId = domain.targetJdId,
            title = domain.title,
            template = domain.template,
            status = domain.status,
        ).also { it.id = domain.id }
    }

}
