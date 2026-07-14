package com.jobdori.infrastructure.persistence.domain.profile.entity

import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Table(name = "profile_certification_v1")
@Entity
class ProfileCertificationEntity(
    @Column(nullable = false)
    var profileId: Long,

    @Column(nullable = false)
    var displayOrder: Int,

    @Column(length = 100)
    var name: String?,

    @Column(length = 100)
    var issuer: String?,

    @Column
    var acquiredAt: LocalDate?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = Certification(
        name = name,
        issuer = issuer,
        acquiredAt = acquiredAt,
    )

    companion object {
        fun from(profileId: Long, displayOrder: Int, domain: Certification) = ProfileCertificationEntity(
            profileId = profileId,
            displayOrder = displayOrder,
            name = domain.name,
            issuer = domain.issuer,
            acquiredAt = domain.acquiredAt,
        )
    }

}
