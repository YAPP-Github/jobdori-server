package com.jobdori.api.application.resume.service

import com.jobdori.api.application.jd.dto.response.JdResponse
import com.jobdori.api.application.experience.service.PdfExperienceImportService
import com.jobdori.api.application.resume.dto.response.ResumeResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.core.application.experience.ExperienceAiExtractionService
import com.jobdori.core.application.experience.ExperienceStarExtractionResult
import com.jobdori.core.application.experience.ExtractedExperienceProject
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeCareerPayload
import com.jobdori.core.domain.resume.ResumeEducationPayload
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeSectionItemPayload
import com.jobdori.core.domain.resume.ResumeSkillPayload
import com.jobdori.core.domain.resume.ResumeAwardPayload
import com.jobdori.core.domain.resume.ResumeCertificatePayload
import com.jobdori.core.domain.resume.ResumeLanguagePayload
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.resume.service.ResumeCreator
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionItemSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionSaveCommand
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PdfResumeImportService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val getJdService: GetJdService,
    private val pdfExperienceImportService: PdfExperienceImportService,
    private val experienceAiExtractionService: ExperienceAiExtractionService,
    private val resumeCreator: ResumeCreator,
) {

    fun importResume(
        file: MultipartFile,
        workspaceId: String,
        userId: Long,
        targetJdId: String? = null,
    ): ResumeResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val text = pdfExperienceImportService.extractText(file = file, userId = userId)
        val extraction = experienceAiExtractionService.extractForResumeImport(text)
        val targetJd = targetJdId?.let { publicId ->
            getJdService.getJd(workspaceId = workspace.id, publicId = publicId)
        }
        val detail = resumeCreator.create(
            workspaceId = workspace.id,
            command = extraction.toResumeSaveCommand(targetJd?.id),
        )
        return ResumeResponse.from(detail, targetJd = targetJd?.let(JdResponse::from))
    }
}

private fun ExperienceStarExtractionResult.toResumeSaveCommand(targetJdId: Long?): ResumeSaveCommand {
    val sections = buildList {
        addSection(ResumeSectionType.BASIC_INFO, listOfNotNull(
            personalInfo.takeIf { it.name.isNotBlank() || it.email.isNotBlank() || it.phone.isNotBlank() }
                ?.let { ResumeBasicInfoPayload(it.name.trim().ifBlank { null }, it.email.trim().ifBlank { null }, it.phone.trim().ifBlank { null }) },
        ))
        addSection(ResumeSectionType.EXPERIENCE, projects.toResumeExperiencePayloads())
        addSection(ResumeSectionType.CAREER, careers.mapNotNull { career ->
            career.toCareer()?.let { ResumeCareerPayload(it.company, it.position, it.period, it.description) }
        })
        addSection(ResumeSectionType.EDUCATION, education.mapNotNull { item ->
            item.toEducation()?.let { ResumeEducationPayload(it.school, it.major, it.degree?.name, it.status?.name, it.period) }
        })
        addSection(ResumeSectionType.LANGUAGE, languageTests.mapNotNull { item ->
            item.toLanguageTest()?.let { ResumeLanguagePayload(it.testName, it.score, it.acquiredAt) }
        })
        addSection(ResumeSectionType.AWARD, awards.mapNotNull { item ->
            item.toAward()?.let { ResumeAwardPayload(it.title, it.organization, it.awardedAt) }
        })
        addSection(ResumeSectionType.CERTIFICATE, certifications.mapNotNull { item ->
            item.toCertification()?.let { ResumeCertificatePayload(it.name, it.issuer, it.acquiredAt) }
        })
        addSection(ResumeSectionType.SKILL, skills.mapNotNull { item ->
            item.toSkill()?.let { ResumeSkillPayload(it.name, it.level?.name) }
        })
    }
    return ResumeSaveCommand(
        targetJdId = targetJdId,
        template = ResumeTemplate.DEFAULT,
        status = ResumeStatus.DRAFT,
        sections = sections,
    )
}

private fun MutableList<ResumeSectionSaveCommand>.addSection(
    type: ResumeSectionType,
    payloads: List<ResumeSectionItemPayload>,
) {
    if (payloads.isEmpty()) return
    add(
        ResumeSectionSaveCommand(
            sectionId = null,
            type = type,
            displayOrder = type.defaultDisplayOrder,
            visible = true,
            items = payloads.mapIndexed { index, payload ->
                ResumeSectionItemSaveCommand(null, payload, (index + 1).toDouble(), true)
            },
        ),
    )
}

private fun List<ExtractedExperienceProject>.toResumeExperiencePayloads(): List<ResumeExperiencePayload> = flatMap { project ->
    val projectName = project.name.trim()
    val projectPeriod = project.period.toPeriod()
    if (project.experiences.isEmpty()) {
        listOfNotNull(
            project.takeIf { projectName.isNotBlank() }?.let {
                ResumeExperiencePayload(projectName, project.role.trim().ifBlank { null }, projectPeriod, project.summary.trim().ifBlank { null })
            },
        )
    } else {
        project.experiences.mapNotNull { experience ->
            val name = experience.title.trim().ifBlank { projectName }.ifBlank { return@mapNotNull null }
            val contents = listOf(
                "상황: ${experience.situation.trim()}",
                "과제: ${experience.task.trim()}",
                "행동: ${experience.action.trim()}",
                "결과: ${experience.result.trim()}",
            ).filterNot { it.endsWith(":") }.joinToString("\n")
            ResumeExperiencePayload(
                name = name,
                role = experience.role.trim().ifBlank { project.role.trim().ifBlank { null } },
                period = experience.period.toPeriod() ?: projectPeriod,
                contents = contents.ifBlank { project.summary.trim().ifBlank { null } },
            )
        }
    }
}
