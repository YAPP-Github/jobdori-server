package com.jobdori.infrastructure.persistence.domain.profile.entity

import com.jobdori.core.domain.profile.section.Career
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Table(name = "profile_career_v1")
@Entity
class ProfileCareerEntity(
    @Column(nullable = false)
    var profileId: Long,

    @Column(nullable = false)
    var displayOrder: Int,

    @Column(length = 100)
    var company: String?,

    @Column(length = 100)
    var position: String?,

    @Column
    var startAt: LocalDate?,

    @Column
    var endAt: LocalDate?,

    @Column(columnDefinition = "text")
    var description: String?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = Career(
        company = company,
        position = position,
        period = toPeriod(startAt, endAt),
        description = description,
    )

    companion object {
        fun from(profileId: Long, displayOrder: Int, domain: Career) = ProfileCareerEntity(
            profileId = profileId,
            displayOrder = displayOrder,
            company = domain.company,
            position = domain.position,
            startAt = domain.period?.startAt,
            endAt = domain.period?.endAt,
            description = domain.description,
        )
    }

}
