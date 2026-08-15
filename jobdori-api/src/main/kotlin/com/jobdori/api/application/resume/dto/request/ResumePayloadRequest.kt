package com.jobdori.api.application.resume.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import com.jobdori.core.domain.profile.ProfilePolicy
import com.jobdori.core.domain.resume.ResumeAwardPayload
import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeCareerPayload
import com.jobdori.core.domain.resume.ResumeCertificatePayload
import com.jobdori.core.domain.resume.ResumeCoreSkillPayload
import com.jobdori.core.domain.resume.ResumeEducationPayload
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeLanguagePayload
import com.jobdori.core.domain.resume.ResumeSectionItemPayload
import com.jobdori.core.domain.resume.ResumeSkillPayload
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

sealed interface ResumePayloadRequest<out T : ResumeSectionItemPayload> {

    fun toPayload(): T

}

data class ResumeBasicInfoPayloadRequest(
    @field:Size(max = 50, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String?,
    @field:Email(message = "이메일 형식이 올바르지 않아요.")
    @field:Size(max = 100, message = "이메일은 최대 {max}자까지 입력할 수 있어요.")
    val email: String?,
    @field:Pattern(regexp = "^$|^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않아요.")
    val phone: String?,
    val hideContact: Boolean = false,
) : ResumePayloadRequest<ResumeBasicInfoPayload> {
    override fun toPayload() = ResumeBasicInfoPayload(
        name = name,
        email = email,
        phone = phone,
        hideContact = hideContact,
    )
}

data class ResumeCoreSkillPayloadRequest(
    @field:Size(max = 2000, message = "내용은 최대 {max}자까지 입력할 수 있어요.")
    val content: String?,
    val isInitialItem: Boolean = false,
) : ResumePayloadRequest<ResumeCoreSkillPayload> {
    override fun toPayload() = ResumeCoreSkillPayload(
        content = content,
        isInitialItem = isInitialItem,
    )
}

data class ResumeCareerPayloadRequest(
    @field:Size(max = 100, message = "회사명은 최대 {max}자까지 입력할 수 있어요.")
    val companyName: String?,
    @field:Size(max = 100, message = "역할은 최대 {max}자까지 입력할 수 있어요.")
    val role: String?,
    @field:Valid
    val period: PeriodRequest?,
    @field:Size(max = 2000, message = "내용은 최대 {max}자까지 입력할 수 있어요.")
    val contents: String?,
) : ResumePayloadRequest<ResumeCareerPayload> {
    override fun toPayload() = ResumeCareerPayload(
        companyName = companyName,
        role = role,
        period = period?.toPeriod(),
        contents = contents,
    )
}

data class ResumeExperiencePayloadRequest(
    @field:Size(max = ProfilePolicy.MAX_TITLE_LENGTH, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String?,
    @field:Size(max = 100, message = "역할은 최대 {max}자까지 입력할 수 있어요.")
    val role: String?,
    @field:Valid
    val period: PeriodRequest?,
    @field:Size(max = ProfilePolicy.MAX_CONTENTS_LENGTH, message = "내용은 최대 {max}자까지 입력할 수 있어요.")
    val contents: String?,
) : ResumePayloadRequest<ResumeExperiencePayload> {
    override fun toPayload() = ResumeExperiencePayload(
        name = name,
        role = role,
        period = period?.toPeriod(),
        contents = contents,
    )
}

data class ResumeEducationPayloadRequest(
    @field:Size(max = 100, message = "학교명은 최대 {max}자까지 입력할 수 있어요.")
    val schoolName: String?,
    @field:Size(max = 100, message = "전공명은 최대 {max}자까지 입력할 수 있어요.")
    val major: String?,
    @field:Size(max = 50, message = "입력 가능한 학위의 최대 길이는 {max}자입니다.")
    val degree: String?,
    @field:Size(max = 50, message = "입력 가능한 졸업 상태의 최대 길이는 {max}자입니다.")
    val status: String?,
    @field:Valid
    val period: PeriodRequest?,
) : ResumePayloadRequest<ResumeEducationPayload> {
    override fun toPayload() = ResumeEducationPayload(
        schoolName = schoolName,
        major = major,
        degree = degree,
        status = status,
        period = period?.toPeriod(),
    )
}

data class ResumeAwardPayloadRequest(
    @field:Size(max = 100, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String?,
    @field:Size(max = 100, message = "기관명은 최대 {max}자까지 입력할 수 있어요.")
    val organization: String?,
    val awardedAt: LocalDate?,
) : ResumePayloadRequest<ResumeAwardPayload> {
    override fun toPayload() = ResumeAwardPayload(
        name = name,
        organization = organization,
        awardedAt = awardedAt,
    )
}

data class ResumeCertificatePayloadRequest(
    @field:Size(max = 100, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String?,
    @field:Size(max = 100, message = "기관명은 최대 {max}자까지 입력할 수 있어요.")
    val organization: String?,
    val acquiredAt: LocalDate?,
) : ResumePayloadRequest<ResumeCertificatePayload> {
    override fun toPayload() = ResumeCertificatePayload(
        name = name,
        organization = organization,
        acquiredAt = acquiredAt,
    )
}

data class ResumeSkillPayloadRequest(
    @field:Size(max = 100, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String?,
    @field:Size(max = 50, message = "입력 가능한 숙련도의 최대 길이는 {max}자입니다.")
    val level: String?,
) : ResumePayloadRequest<ResumeSkillPayload> {
    override fun toPayload() = ResumeSkillPayload(name = name, level = level)
}

data class ResumeLanguagePayloadRequest(
    @field:Size(max = 100, message = "시험명은 최대 {max}자까지 입력할 수 있어요.")
    val examName: String?,
    @field:Size(max = 50, message = "점수 및 등급은 최대 {max}자까지 입력할 수 있어요.")
    val scoreOrGrade: String?,
    val acquiredAt: LocalDate?,
) : ResumePayloadRequest<ResumeLanguagePayload> {
    override fun toPayload() = ResumeLanguagePayload(
        examName = examName,
        scoreOrGrade = scoreOrGrade,
        acquiredAt = acquiredAt,
    )
}
