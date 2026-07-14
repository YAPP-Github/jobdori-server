package com.jobdori.api.application.profile.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import com.jobdori.core.domain.profile.section.Award
import com.jobdori.core.domain.profile.section.Career
import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.core.domain.profile.section.Degree
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.EducationStatus
import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.core.domain.profile.section.ProfileSkill
import com.jobdori.core.domain.profile.section.SkillLevel
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ProfileEducationRequest(
    @field:Size(max = 100)
    val school: String? = null,

    @field:Size(max = 100)
    val major: String? = null,

    val degree: Degree? = null,

    val status: EducationStatus? = null,

    @field:Valid
    val period: PeriodRequest? = null,
) {
    fun toDomain() = Education(
        school = school,
        major = major,
        degree = degree,
        status = status,
        period = period?.toPeriod(),
    )
}

data class ProfileCareerRequest(
    @field:Size(max = 100)
    val company: String? = null,

    @field:Size(max = 100)
    val position: String? = null,

    @field:Valid
    val period: PeriodRequest? = null,

    @field:Size(max = 500)
    val description: String? = null,
) {
    fun toDomain() = Career(
        company = company,
        position = position,
        period = period?.toPeriod(),
        description = description,
    )
}

data class ProfileLanguageTestRequest(
    @field:Size(max = 100)
    val testName: String? = null,

    @field:Size(max = 50)
    val score: String? = null,

    val acquiredAt: LocalDate? = null,
) {
    fun toDomain() = LanguageTest(
        testName = testName,
        score = score,
        acquiredAt = acquiredAt,
    )
}

data class ProfileAwardRequest(
    @field:Size(max = 100)
    val title: String? = null,

    @field:Size(max = 100)
    val organization: String? = null,

    val awardedAt: LocalDate? = null,
) {
    fun toDomain() = Award(
        title = title,
        organization = organization,
        awardedAt = awardedAt,
    )
}

data class ProfileCertificationRequest(
    @field:Size(max = 100)
    val name: String? = null,

    @field:Size(max = 100)
    val issuer: String? = null,

    val acquiredAt: LocalDate? = null,
) {
    fun toDomain() = Certification(
        name = name,
        issuer = issuer,
        acquiredAt = acquiredAt,
    )
}

data class ProfileSkillRequest(
    @field:Size(max = 100)
    val name: String? = null,

    val level: SkillLevel? = null,
) {
    fun toDomain() = ProfileSkill(
        name = name,
        level = level,
    )
}
