package com.jobdori.infrastructure.persistence.domain.resume.entity

import com.jobdori.core.domain.resume.ResumeSection
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Table(name = "resume_section_v1")
@Entity
class ResumeSectionEntity(
    @Column(nullable = false)
    var resumeId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: ResumeSectionType,

    @Column(nullable = false, precision = 20, scale = 10)
    var displayOrder: BigDecimal,

    @Column(nullable = false)
    var visible: Boolean,

    ) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = ResumeSection(
        id = id,
        resumeId = resumeId,
        type = type,
        displayOrder = displayOrder,
        visible = visible,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun from(domain: ResumeSection) = ResumeSectionEntity(
            resumeId = domain.resumeId,
            type = domain.type,
            displayOrder = domain.displayOrder,
            visible = domain.visible,
        ).also { it.id = domain.id }
    }

}
