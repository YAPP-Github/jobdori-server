package com.jobdori.api.application.resume.dto.request

import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.domain.resume.ResumeSectionItemPayload
import jakarta.validation.Valid

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
