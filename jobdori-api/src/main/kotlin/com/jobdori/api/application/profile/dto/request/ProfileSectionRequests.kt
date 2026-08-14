package com.jobdori.api.application.profile.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import com.jobdori.core.domain.profile.ProfilePolicy
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
    @field:Size(max = 100, message = "입력 가능한 학교명의 최대 길이는 {max}자입니다.")
    val school: String? = null,

    @field:Size(max = 100, message = "입력 가능한 전공의 최대 길이는 {max}자입니다.")
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
    @field:Size(max = 100, message = "입력 가능한 회사명의 최대 길이는 {max}자입니다.")
    val company: String? = null,

    @field:Size(max = 100, message = "입력 가능한 직무의 최대 길이는 {max}자입니다.")
    val position: String? = null,

    @field:Valid
    val period: PeriodRequest? = null,

    @field:Size(max = ProfilePolicy.MAX_CAREER_DESCRIPTION_LENGTH, message = "입력 가능한 설명의 최대 길이는 {max}자입니다.")
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
    @field:Size(max = 100, message = "입력 가능한 어학 시험명의 최대 길이는 {max}자입니다.")
    val testName: String? = null,

    @field:Size(max = 50, message = "입력 가능한 점수의 최대 길이는 {max}자입니다.")
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
    @field:Size(max = 100, message = "입력 가능한 제목의 최대 길이는 {max}자입니다.")
    val title: String? = null,

    @field:Size(max = 100, message = "입력 가능한 기관명의 최대 길이는 {max}자입니다.")
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
    @field:Size(max = 100, message = "입력 가능한 이름의 최대 길이는 {max}자입니다.")
    val name: String? = null,

    @field:Size(max = 100, message = "입력 가능한 발급 기관명의 최대 길이는 {max}자입니다.")
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
    @field:Size(max = 100, message = "입력 가능한 이름의 최대 길이는 {max}자입니다.")
    val name: String? = null,

    val level: SkillLevel? = null,
) {
    fun toDomain() = ProfileSkill(
        name = name,
        level = level,
    )
}
