package com.jobdori.api.application.resume.dto.response

import com.jobdori.api.application.common.dto.response.PeriodResponse
import com.jobdori.core.domain.resume.ResumeAwardPayload
import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeCareerPayload
import com.jobdori.core.domain.resume.ResumeCertificatePayload
import com.jobdori.core.domain.resume.ResumeCoreSkillPayload
import com.jobdori.core.domain.resume.ResumeEducationPayload
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeLanguagePayload
import com.jobdori.core.domain.resume.ResumeSkillPayload
import java.time.LocalDate

sealed interface ResumePayloadResponse

data class ResumeBasicInfoPayloadResponse(
    val name: String?,
    val email: String?,
    val phone: String?,
    val hideContact: Boolean,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeBasicInfoPayload) = ResumeBasicInfoPayloadResponse(
            name = payload.name,
            email = payload.email,
            phone = payload.phone,
            hideContact = payload.hideContact,
        )
    }
}

data class ResumeCoreSkillPayloadResponse(
    val content: String?,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeCoreSkillPayload) = ResumeCoreSkillPayloadResponse(content = payload.content)
    }
}

data class ResumeCareerPayloadResponse(
    val companyName: String?,
    val role: String?,
    val period: PeriodResponse?,
    val contents: String?,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeCareerPayload) = ResumeCareerPayloadResponse(
            companyName = payload.companyName,
            role = payload.role,
            period = payload.period?.let { PeriodResponse.from(it) },
            contents = payload.contents,
        )
    }
}

data class ResumeExperiencePayloadResponse(
    val name: String?,
    val role: String?,
    val period: PeriodResponse?,
    val contents: String?,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeExperiencePayload) = ResumeExperiencePayloadResponse(
            name = payload.name,
            role = payload.role,
            period = payload.period?.let { PeriodResponse.from(it) },
            contents = payload.contents,
        )
    }
}

data class ResumeEducationPayloadResponse(
    val schoolName: String?,
    val major: String?,
    val degree: String?,
    val status: String?,
    val period: PeriodResponse?,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeEducationPayload) = ResumeEducationPayloadResponse(
            schoolName = payload.schoolName,
            major = payload.major,
            degree = payload.degree,
            status = payload.status,
            period = payload.period?.let { PeriodResponse.from(it) },
        )
    }
}

data class ResumeAwardPayloadResponse(
    val name: String?,
    val organization: String?,
    val awardedAt: LocalDate?,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeAwardPayload) = ResumeAwardPayloadResponse(
            name = payload.name,
            organization = payload.organization,
            awardedAt = payload.awardedAt,
        )
    }
}

data class ResumeCertificatePayloadResponse(
    val name: String?,
    val organization: String?,
    val acquiredAt: LocalDate?,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeCertificatePayload) = ResumeCertificatePayloadResponse(
            name = payload.name,
            organization = payload.organization,
            acquiredAt = payload.acquiredAt,
        )
    }
}

data class ResumeSkillPayloadResponse(
    val name: String?,
    val level: String?,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeSkillPayload) = ResumeSkillPayloadResponse(name = payload.name, level = payload.level)
    }
}

data class ResumeLanguagePayloadResponse(
    val examName: String?,
    val scoreOrGrade: String?,
    val acquiredAt: LocalDate?,
) : ResumePayloadResponse {
    companion object {
        fun from(payload: ResumeLanguagePayload) = ResumeLanguagePayloadResponse(
            examName = payload.examName,
            scoreOrGrade = payload.scoreOrGrade,
            acquiredAt = payload.acquiredAt,
        )
    }
}
