package com.jobdori.infrastructure.persistence.domain.profile.entity

import com.jobdori.common.model.Period
import com.jobdori.core.domain.profile.section.Degree
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.EducationStatus
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Table(name = "profile_education_v1")
@Entity
class ProfileEducationEntity(
    @Column(nullable = false)
    var profileId: Long,

    @Column(nullable = false)
    var displayOrder: Int,

    @Column(length = 100)
    var school: String?,

    @Column(length = 100)
    var major: String?,

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    var degree: Degree?,

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    var status: EducationStatus?,

    @Column
    var startAt: LocalDate?,

    @Column
    var endAt: LocalDate?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = Education(
        school = school,
        major = major,
        degree = degree,
        status = status,
        period = toPeriod(startAt, endAt),
    )

    companion object {
        fun from(profileId: Long, displayOrder: Int, domain: Education) = ProfileEducationEntity(
            profileId = profileId,
            displayOrder = displayOrder,
            school = domain.school,
            major = domain.major,
            degree = domain.degree,
            status = domain.status,
            startAt = domain.period?.startAt,
            endAt = domain.period?.endAt,
        )
    }

}

internal fun toPeriod(startAt: LocalDate?, endAt: LocalDate?): Period? {
    if (startAt == null && endAt == null) {
        return null
    }

    return Period(startAt = startAt, endAt = endAt)
}
