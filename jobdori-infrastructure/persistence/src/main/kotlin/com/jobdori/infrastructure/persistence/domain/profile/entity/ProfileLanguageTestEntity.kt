package com.jobdori.infrastructure.persistence.domain.profile.entity

import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Table(name = "profile_language_test_v1")
@Entity
class ProfileLanguageTestEntity(
    @Column(nullable = false)
    var profileId: Long,

    @Column(nullable = false)
    var displayOrder: Int,

    @Column(length = 100)
    var testName: String?,

    @Column(length = 50)
    var score: String?,

    @Column
    var acquiredAt: LocalDate?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = LanguageTest(
        testName = testName,
        score = score,
        acquiredAt = acquiredAt,
    )

    companion object {
        fun from(profileId: Long, displayOrder: Int, domain: LanguageTest) = ProfileLanguageTestEntity(
            profileId = profileId,
            displayOrder = displayOrder,
            testName = domain.testName,
            score = domain.score,
            acquiredAt = domain.acquiredAt,
        )
    }

}
