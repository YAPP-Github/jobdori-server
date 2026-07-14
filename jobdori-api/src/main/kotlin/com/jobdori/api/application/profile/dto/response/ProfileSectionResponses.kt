package com.jobdori.api.application.profile.dto.response

import com.jobdori.api.application.common.dto.response.PeriodResponse
import com.jobdori.core.domain.profile.section.Award
import com.jobdori.core.domain.profile.section.Career
import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.core.domain.profile.section.Degree
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.EducationStatus
import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.core.domain.profile.section.ProfileSkill
import com.jobdori.core.domain.profile.section.SkillLevel
import java.time.LocalDate

data class ProfileEducationResponse(
    val school: String?,
    val major: String?,
    val degree: Degree?,
    val status: EducationStatus?,
    val period: PeriodResponse?,
) {
    companion object {
        fun from(domain: Education) = ProfileEducationResponse(
            school = domain.school,
            major = domain.major,
            degree = domain.degree,
            status = domain.status,
            period = domain.period?.let { PeriodResponse.from(it) },
        )
    }
}

data class ProfileCareerResponse(
    val company: String?,
    val position: String?,
    val period: PeriodResponse?,
    val description: String?,
) {
    companion object {
        fun from(domain: Career) = ProfileCareerResponse(
            company = domain.company,
            position = domain.position,
            period = domain.period?.let { PeriodResponse.from(it) },
            description = domain.description,
        )
    }
}

data class ProfileLanguageTestResponse(
    val testName: String?,
    val score: String?,
    val acquiredAt: LocalDate?,
) {
    companion object {
        fun from(domain: LanguageTest) = ProfileLanguageTestResponse(
            testName = domain.testName,
            score = domain.score,
            acquiredAt = domain.acquiredAt,
        )
    }
}

data class ProfileAwardResponse(
    val title: String?,
    val organization: String?,
    val awardedAt: LocalDate?,
) {
    companion object {
        fun from(domain: Award) = ProfileAwardResponse(
            title = domain.title,
            organization = domain.organization,
            awardedAt = domain.awardedAt,
        )
    }
}

data class ProfileCertificationResponse(
    val name: String?,
    val issuer: String?,
    val acquiredAt: LocalDate?,
) {
    companion object {
        fun from(domain: Certification) = ProfileCertificationResponse(
            name = domain.name,
            issuer = domain.issuer,
            acquiredAt = domain.acquiredAt,
        )
    }
}

data class ProfileSkillResponse(
    val name: String?,
    val level: SkillLevel?,
) {
    companion object {
        fun from(domain: ProfileSkill) = ProfileSkillResponse(
            name = domain.name,
            level = domain.level,
        )
    }
}
