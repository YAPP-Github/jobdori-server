package com.jobdori.infrastructure.persistence.domain.profile.entity

import com.jobdori.core.domain.profile.section.Award
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Table(name = "profile_award_v1")
@Entity
class ProfileAwardEntity(
    @Column(nullable = false)
    var profileId: Long,

    @Column(nullable = false)
    var displayOrder: Int,

    @Column(length = 100)
    var title: String?,

    @Column(length = 100)
    var organization: String?,

    @Column
    var awardedAt: LocalDate?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = Award(
        title = title,
        organization = organization,
        awardedAt = awardedAt,
    )

    companion object {
        fun from(profileId: Long, displayOrder: Int, domain: Award) = ProfileAwardEntity(
            profileId = profileId,
            displayOrder = displayOrder,
            title = domain.title,
            organization = domain.organization,
            awardedAt = domain.awardedAt,
        )
    }

}
