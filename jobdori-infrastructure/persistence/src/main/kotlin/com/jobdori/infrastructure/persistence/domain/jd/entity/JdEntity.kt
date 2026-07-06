package com.jobdori.infrastructure.persistence.domain.jd.entity

import com.jobdori.core.domain.jd.Jd
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Table(name = "jd_v1")
@Entity
class JdEntity(
    @Column(nullable = false, length = 50, unique = true, updatable = false)
    var publicId: String,

    @Column(nullable = false)
    var workspaceId: Long,

    @Column(columnDefinition = "text")
    var sourceUrl: String?,

    @Column(nullable = false)
    var companyName: String,

    @Column(nullable = false)
    var positionTitle: String,

    @Column(nullable = false, columnDefinition = "text")
    var companyIntro: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var responsibilities: List<String>,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var requiredExperiences: List<String>,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var preferredExperiences: List<String>,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var hiringProcess: List<String>,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = Jd(
        id = id,
        publicId = publicId,
        workspaceId = workspaceId,
        sourceUrl = sourceUrl,
        companyName = companyName,
        positionTitle = positionTitle,
        companyIntro = companyIntro,
        responsibilities = responsibilities,
        requiredExperiences = requiredExperiences,
        preferredExperiences = preferredExperiences,
        hiringProcess = hiringProcess,
        createdAt = createdAt,
    )

    companion object {
        fun from(jd: Jd) = JdEntity(
            publicId = jd.publicId,
            workspaceId = jd.workspaceId,
            sourceUrl = jd.sourceUrl,
            companyName = jd.companyName,
            positionTitle = jd.positionTitle,
            companyIntro = jd.companyIntro,
            responsibilities = jd.responsibilities,
            requiredExperiences = jd.requiredExperiences,
            preferredExperiences = jd.preferredExperiences,
            hiringProcess = jd.hiringProcess,
        ).also { it.id = jd.id }
    }

}
