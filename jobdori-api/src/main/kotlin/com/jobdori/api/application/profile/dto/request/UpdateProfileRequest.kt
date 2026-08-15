package com.jobdori.api.application.profile.dto.request

import com.jobdori.core.domain.profile.ProfilePolicy
import com.jobdori.core.domain.profile.service.command.ProfileUpdateCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

// null = 미변경, 빈 리스트/빈 문자열 = 비우기
data class UpdateProfileRequest(
    @field:Size(max = 50, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String? = null,

    // 형식은 동료 ResumeBasicInfoPayload와 동일. 빈 문자열은 "비우기" 의미라 허용
    @field:Pattern(regexp = """^$|^\d{2,3}-\d{3,4}-\d{4}$""", message = "전화번호 형식이 올바르지 않아요.")
    val phone: String? = null,

    @field:Size(max = 100, message = "이메일은 최대 {max}자까지 입력할 수 있어요.")
    val email: String? = null,

    @field:Size(max = ProfilePolicy.MAX_CORE_COMPETENCY_LENGTH, message = "핵심 역량은 최대 {max}자까지 입력할 수 있어요.")
    val coreCompetency: String? = null,

    @field:Valid
    val educations: List<ProfileEducationRequest>? = null,

    @field:Valid
    val careers: List<ProfileCareerRequest>? = null,

    @field:Valid
    val languageTests: List<ProfileLanguageTestRequest>? = null,

    @field:Valid
    val awards: List<ProfileAwardRequest>? = null,

    @field:Valid
    val certifications: List<ProfileCertificationRequest>? = null,

    @field:Valid
    val skills: List<ProfileSkillRequest>? = null,
) {

    fun toCommand() = ProfileUpdateCommand(
        name = name,
        phone = phone,
        email = email,
        coreCompetency = coreCompetency,
        educations = educations?.map { it.toDomain() },
        careers = careers?.map { it.toDomain() },
        languageTests = languageTests?.map { it.toDomain() },
        awards = awards?.map { it.toDomain() },
        certifications = certifications?.map { it.toDomain() },
        skills = skills?.map { it.toDomain() },
    )

}
