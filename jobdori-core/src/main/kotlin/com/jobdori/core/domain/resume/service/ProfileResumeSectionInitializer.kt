package com.jobdori.core.domain.resume.service

import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.resume.ResumeAwardPayload
import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeCareerPayload
import com.jobdori.core.domain.resume.ResumeCertificatePayload
import com.jobdori.core.domain.resume.ResumeCoreSkillPayload
import com.jobdori.core.domain.resume.ResumeEducationPayload
import com.jobdori.core.domain.resume.ResumeLanguagePayload
import com.jobdori.core.domain.resume.ResumeSectionItemPayload
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeSkillPayload
import com.jobdori.core.domain.resume.service.command.ResumeSectionItemSaveCommand
import org.springframework.stereotype.Component

@Component
class ProfileResumeSectionInitializer {

    fun initializeItems(
        detail: ProfileDetail,
        type: ResumeSectionType,
    ): List<ResumeSectionItemSaveCommand> = payloads(detail, type).mapIndexed { index, payload ->
        ResumeSectionItemSaveCommand(
            itemId = null,
            payload = payload,
            displayOrder = (index + 1).toDouble(),
            visible = true,
        )
    }

    private fun payloads(detail: ProfileDetail, type: ResumeSectionType): List<ResumeSectionItemPayload> = when (type) {
        ResumeSectionType.BASIC_INFO -> listOfNotNull(
            detail.profile.name.nonBlank()?.let {
                ResumeBasicInfoPayload(it, detail.profile.email, detail.profile.phone)
            },
        )
        ResumeSectionType.CORE_SKILL -> listOfNotNull(
            detail.profile.coreCompetency.nonBlank()?.let {
                ResumeCoreSkillPayload(content = it, isInitialItem = true)
            },
        )
        ResumeSectionType.CAREER -> detail.sections.careers.mapNotNull { career ->
                career.company.nonBlank()?.let { company ->
                    ResumeCareerPayload(company, career.position, career.period, career.description.orEmpty())
                }
            }
        ResumeSectionType.EDUCATION -> detail.sections.educations.mapNotNull { education ->
                education.school.nonBlank()?.let { school ->
                    ResumeEducationPayload(
                        schoolName = school,
                        major = education.major,
                        degree = education.degree?.name,
                        status = education.status?.name,
                        period = education.period,
                    )
                }
            }
        ResumeSectionType.AWARD -> detail.sections.awards.mapNotNull { award ->
                award.title.nonBlank()?.let { ResumeAwardPayload(it, award.organization, award.awardedAt) }
            }
        ResumeSectionType.LANGUAGE -> detail.sections.languageTests.mapNotNull { language ->
                val name = language.testName.nonBlank()
                val score = language.score.nonBlank()
                if (name != null && score != null) ResumeLanguagePayload(name, score, language.acquiredAt) else null
            }
        ResumeSectionType.CERTIFICATE -> detail.sections.certifications.mapNotNull { certification ->
                certification.name.nonBlank()?.let {
                    ResumeCertificatePayload(it, certification.issuer, certification.acquiredAt)
                }
            }
        ResumeSectionType.SKILL -> detail.sections.skills.mapNotNull { skill ->
                skill.name.nonBlank()?.let { ResumeSkillPayload(it, skill.level?.name) }
            }
        ResumeSectionType.EXPERIENCE -> emptyList()
    }

    private fun String?.nonBlank(): String? = this?.takeIf { it.isNotBlank() }
}
