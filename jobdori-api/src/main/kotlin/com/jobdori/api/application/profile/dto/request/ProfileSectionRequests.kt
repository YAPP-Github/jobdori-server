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
    @field:Size(max = 100, message = "학교명은 최대 {max}자까지 입력할 수 있어요.")
    val school: String? = null,

    @field:Size(max = 100, message = "전공명은 최대 {max}자까지 입력할 수 있어요.")
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
    @field:Size(max = 100, message = "회사명은 최대 {max}자까지 입력할 수 있어요.")
    val company: String? = null,

    @field:Size(max = 100, message = "직무명은 최대 {max}자까지 입력할 수 있어요.")
    val position: String? = null,

    @field:Valid
    val period: PeriodRequest? = null,

    @field:Size(max = ProfilePolicy.MAX_CAREER_DESCRIPTION_LENGTH, message = "세부 내용은 최대 {max}자까지 입력할 수 있어요.")
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
    @field:Size(max = 100, message = "시험명은 최대 {max}자까지 입력할 수 있어요.")
    val testName: String? = null,

    @field:Size(max = 50, message = "점수는 최대 {max}자까지 입력할 수 있어요.")
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
    @field:Size(max = 100, message = "제목은 최대 {max}자까지 입력할 수 있어요.")
    val title: String? = null,

    @field:Size(max = 100, message = "기관명은 최대 {max}자까지 입력할 수 있어요.")
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
    @field:Size(max = 100, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String? = null,

    @field:Size(max = 100, message = "발급 기관명은 최대 {max}자까지 입력할 수 있어요.")
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
    @field:Size(max = 100, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String? = null,

    val level: SkillLevel? = null,
) {
    fun toDomain() = ProfileSkill(
        name = name,
        level = level,
    )
}
