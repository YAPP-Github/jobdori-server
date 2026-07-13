package com.jobdori.api.application.resume.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.InvalidArgumentsException
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
import java.time.LocalDate

fun interface ResumePayloadRequest<out T : ResumeSectionItemPayload> {

    fun toPayload(): T

}

data class ResumeSectionItemPayloadRequest(
    @field:Valid
    val basicInfo: ResumeBasicInfoPayloadRequest?,
    @field:Valid
    val coreSkill: ResumeCoreSkillPayloadRequest?,
    @field:Valid
    val career: ResumeCareerPayloadRequest?,
    @field:Valid
    val experience: ResumeExperiencePayloadRequest?,
    @field:Valid
    val education: ResumeEducationPayloadRequest?,
    @field:Valid
    val award: ResumeAwardPayloadRequest?,
    @field:Valid
    val certificate: ResumeCertificatePayloadRequest?,
    @field:Valid
    val language: ResumeLanguagePayloadRequest?,
    @field:Valid
    val skill: ResumeSkillPayloadRequest?,
) {

    fun toPayload(): ResumeSectionItemPayload {
        val payloads = listOfNotNull(
            basicInfo?.toPayload(),
            coreSkill?.toPayload(),
            career?.toPayload(),
            experience?.toPayload(),
            education?.toPayload(),
            award?.toPayload(),
            certificate?.toPayload(),
            language?.toPayload(),
            skill?.toPayload(),
        )
        if (payloads.size != 1) {
            throw InvalidArgumentsException(
                message = "payload는 정확히 하나의 타입만 입력해야 합니다. [count=${payloads.size}]",
                details = listOf(payloadErrorDetail(payloads.size)),
            )
        }
        return payloads.single()
    }

    private fun payloadErrorDetail(payloadCount: Int): ErrorDetail {
        val reason = if (payloadCount == 0) {
            "payload 하위 필드 중 하나를 입력해야 합니다."
        } else {
            "payload 하위 필드는 하나만 입력할 수 있습니다."
        }
        return ErrorDetail(field = "payload", reason = reason)
    }

}

data class ResumeBasicInfoPayloadRequest(
    val name: String,
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String?,
    @field:Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)")
    val phone: String?,
) : ResumePayloadRequest<ResumeBasicInfoPayload> {
    override fun toPayload() = ResumeBasicInfoPayload(
        name = name,
        email = email,
        phone = phone,
    )
}

data class ResumeCoreSkillPayloadRequest(
    val content: String,
) : ResumePayloadRequest<ResumeCoreSkillPayload> {
    override fun toPayload() = ResumeCoreSkillPayload(content = content)
}

data class ResumeCareerPayloadRequest(
    val companyName: String,
    val role: String?,
    val period: PeriodRequest?,
    val contents: String,
) : ResumePayloadRequest<ResumeCareerPayload> {
    override fun toPayload() = ResumeCareerPayload(
        companyName = companyName,
        role = role,
        period = period?.toPeriod(),
        contents = contents,
    )
}

data class ResumeExperiencePayloadRequest(
    val name: String,
    val role: String?,
    val period: PeriodRequest?,
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
    val schoolName: String,
    val major: String?,
    val degree: String?,
    val status: String?,
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
    val name: String,
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
    val name: String,
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
    val name: String,
    val level: String?,
) : ResumePayloadRequest<ResumeSkillPayload> {
    override fun toPayload() = ResumeSkillPayload(name = name, level = level)
}

data class ResumeLanguagePayloadRequest(
    val examName: String,
    val scoreOrGrade: String,
    val acquiredAt: LocalDate?,
) : ResumePayloadRequest<ResumeLanguagePayload> {
    override fun toPayload() = ResumeLanguagePayload(
        examName = examName,
        scoreOrGrade = scoreOrGrade,
        acquiredAt = acquiredAt,
    )
}
