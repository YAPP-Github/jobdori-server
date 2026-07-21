package com.jobdori.api.application.resume.dto.response

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

data class ResumeSectionItemPayloadResponse(
    val basicInfo: ResumeBasicInfoPayloadResponse?,
    val coreSkill: ResumeCoreSkillPayloadResponse?,
    val career: ResumeCareerPayloadResponse?,
    val experience: ResumeExperiencePayloadResponse?,
    val education: ResumeEducationPayloadResponse?,
    val award: ResumeAwardPayloadResponse?,
    val certificate: ResumeCertificatePayloadResponse?,
    val language: ResumeLanguagePayloadResponse?,
    val skill: ResumeSkillPayloadResponse?,
) {

    companion object {
        fun from(payload: ResumeSectionItemPayload): ResumeSectionItemPayloadResponse {
            return ResumeSectionItemPayloadResponse(
                basicInfo = (payload as? ResumeBasicInfoPayload)?.let { ResumeBasicInfoPayloadResponse.from(it) },
                coreSkill = (payload as? ResumeCoreSkillPayload)?.let { ResumeCoreSkillPayloadResponse.from(it) },
                career = (payload as? ResumeCareerPayload)?.let { ResumeCareerPayloadResponse.from(it) },
                experience = (payload as? ResumeExperiencePayload)?.let { ResumeExperiencePayloadResponse.from(it) },
                education = (payload as? ResumeEducationPayload)?.let { ResumeEducationPayloadResponse.from(it) },
                award = (payload as? ResumeAwardPayload)?.let { ResumeAwardPayloadResponse.from(it) },
                certificate = (payload as? ResumeCertificatePayload)?.let { ResumeCertificatePayloadResponse.from(it) },
                language = (payload as? ResumeLanguagePayload)?.let { ResumeLanguagePayloadResponse.from(it) },
                skill = (payload as? ResumeSkillPayload)?.let { ResumeSkillPayloadResponse.from(it) },
            )
        }
    }

}
