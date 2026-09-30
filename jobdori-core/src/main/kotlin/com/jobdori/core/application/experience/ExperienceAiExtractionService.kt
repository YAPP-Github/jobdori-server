package com.jobdori.core.application.experience

import com.jobdori.common.model.Period
import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.experience.command.ImportedExperienceCommandGroup
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.section.Award
import com.jobdori.core.domain.profile.section.Career
import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.core.domain.profile.section.Degree
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.EducationStatus
import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.core.domain.profile.section.ProfileSkill
import com.jobdori.core.domain.profile.section.SkillLevel
import com.jobdori.core.domain.profile.service.command.ProfileUpdateCommand
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class ExperienceAiExtractionService(
    private val aiChatClient: AiChatClient,
    private val promptTemplateRepository: PromptTemplateRepository,
) {

    fun extract(pdfText: String): ExperienceStarExtractionResult {
        return extractWithPrompt(
            pdfText = pdfText,
            promptType = PromptType.EXPERIENCE_STAR_EXTRACTION,
            missingPromptMessage = "경험 추출 프롬프트가 없습니다.",
            userPrompt = buildUserPrompt(pdfText),
        )
    }

    fun extractForResumeImport(pdfText: String): ExperienceStarExtractionResult = extractWithPrompt(
        pdfText = pdfText,
        promptType = PromptType.RESUME_IMPORT_EXTRACTION,
        missingPromptMessage = "이력서 가져오기 추출 프롬프트가 없습니다.",
        userPrompt = buildResumeImportUserPrompt(pdfText),
    )

    private fun extractWithPrompt(
        pdfText: String,
        promptType: PromptType,
        missingPromptMessage: String,
        userPrompt: String,
    ): ExperienceStarExtractionResult {
        val prompt = promptTemplateRepository.findByType(promptType)
            ?: throw AiException(
                message = missingPromptMessage,
                errorCode = AiErrorCode.E500_AI_GENERATION_FAILED,
            )

        return aiChatClient.generateStructured(
            prompt.buildStructured(
                userPrompt = userPrompt,
                responseType = ExperienceStarExtractionResult::class,
            ),
        )
    }

    private fun buildUserPrompt(pdfText: String): String {
        return """
            다음 PDF 추출 텍스트에서 프로필 정보(인적사항/학력/경력/어학/수상/자격증/기술)와 프로젝트/경험을 추출해라.
            원문에 등장하는 모든 프로젝트와 경험을 빠짐없이 반환하고, 원문에 없는 사실은 만들지 마라.
            성과 수치나 기간이 없다는 이유로 항목을 빼지 말고, 프로젝트명을 특정할 수 없는 항목만 제외해라.

            [PDF_TEXT]
            $pdfText
        """.trimIndent()
    }

    private fun buildResumeImportUserPrompt(pdfText: String): String {
        return """
            다음 PDF 이력서에서 인적사항, 학력, 경력, 어학, 수상, 자격증, 기술 스택과 프로젝트/경험을 구조화해라.

            이 작업의 최우선순위는 원문 보존이다.
            - 원문의 제목, 회사명, 역할, 기간, 설명, 수치, 기술명과 고유명사를 가능한 한 그대로 복사해라.
            - 문장을 요약, 교정, 미사여구 추가, STAR 재작성, 이력서 문장 생성하지 마라.
            - situation/task/action/result는 원문 문장 또는 원문 bullet을 의미상 해당 필드에 배치하되 표현을 바꾸지 마라.
            - 원문에 없는 사실, 수치, 기간, 역할, 성과를 추론하거나 생성하지 마라.
            - 원문 정보가 어느 필드에 속하는지 불명확하면 임의로 보정하지 말고 가장 가까운 필드에 원문 그대로 넣어라.
            - 누락된 값은 빈 문자열 또는 null로 두고, 원문에 있는 항목은 정보가 부족해도 삭제하지 마라.

            [PDF_TEXT]
            $pdfText
        """.trimIndent()
    }

}

data class ExperienceStarExtractionResult(
    val personalInfo: PersonalInfo = PersonalInfo(),
    val education: List<ExtractedEducation> = emptyList(),
    val careers: List<ExtractedCareer> = emptyList(),
    val languageTests: List<ExtractedLanguageTest> = emptyList(),
    val awards: List<ExtractedAward> = emptyList(),
    val certifications: List<ExtractedCertification> = emptyList(),
    val skills: List<ExtractedSkill> = emptyList(),
    val projects: List<ExtractedExperienceProject> = emptyList(),
) {

    fun toCommandGroups(): List<ImportedExperienceCommandGroup> {
        return projects.mapNotNull { project -> project.toCommandGroup() }
    }

    // 사용자가 이미 입력한 값 보호: 비어 있는 필드/섹션만 채운다 (null = 미변경)
    fun toProfileUpdateCommand(current: ProfileDetail): ProfileUpdateCommand {
        return ProfileUpdateCommand(
            name = personalInfo.name.extractedIfMissing(current.profile.name),
            phone = personalInfo.phone.extractedIfMissing(current.profile.phone),
            email = personalInfo.email.extractedIfMissing(current.profile.email),
            educations = education.mapNotNull { it.toEducation() }.extractedIfMissing(current.sections.educations),
            careers = careers.mapNotNull { it.toCareer() }.extractedIfMissing(current.sections.careers),
            languageTests = languageTests.mapNotNull { it.toLanguageTest() }
                .extractedIfMissing(current.sections.languageTests),
            awards = awards.mapNotNull { it.toAward() }.extractedIfMissing(current.sections.awards),
            certifications = certifications.mapNotNull { it.toCertification() }
                .extractedIfMissing(current.sections.certifications),
            skills = skills.mapNotNull { it.toSkill() }.extractedIfMissing(current.sections.skills),
        )
    }

    private fun String.extractedIfMissing(currentValue: String?): String? {
        return trim().takeIf { it.isNotBlank() && currentValue.isNullOrBlank() }
    }

    private fun <T> List<T>.extractedIfMissing(currentValues: List<*>): List<T>? {
        return takeIf { it.isNotEmpty() && currentValues.isEmpty() }
    }

    data class PersonalInfo(
        val name: String = "",
        val phone: String = "",
        val email: String = "",
    )

}

data class ExtractedEducation(
    val school: String = "",
    val major: String = "",
    val degree: String = "",
    val status: String = "",
    val period: ExtractedPeriod = ExtractedPeriod(),
    val periodText: String = "",
) {

    fun toEducation(): Education? {
        val normalizedSchool = school.trim().ifBlank { return null }
        return Education(
            school = normalizedSchool,
            major = major.trim().ifBlank { null },
            degree = degree.toEnumOrNull<Degree>(),
            status = status.toEnumOrNull<EducationStatus>(),
            period = period.toPeriod() ?: PeriodParser.parse(periodText),
        )
    }

}

data class ExtractedCareer(
    val company: String = "",
    val position: String = "",
    val period: ExtractedPeriod = ExtractedPeriod(),
    val periodText: String = "",
    val description: String = "",
) {

    fun toCareer(): Career? {
        val normalizedCompany = company.trim().ifBlank { return null }
        return Career(
            company = normalizedCompany,
            position = position.trim().ifBlank { null },
            period = period.toPeriod() ?: PeriodParser.parse(periodText),
            description = description.trim().ifBlank { null },
        )
    }

}

data class ExtractedLanguageTest(
    val testName: String = "",
    val score: String = "",
    val acquiredAt: ExtractedDate = ExtractedDate(),
) {

    fun toLanguageTest(): LanguageTest? {
        val normalizedTestName = testName.trim().ifBlank { return null }
        return LanguageTest(
            testName = normalizedTestName,
            score = score.trim().ifBlank { null },
            acquiredAt = acquiredAt.toLocalDate(),
        )
    }

}

data class ExtractedAward(
    val title: String = "",
    val organization: String = "",
    val awardedAt: ExtractedDate = ExtractedDate(),
) {

    fun toAward(): Award? {
        val normalizedTitle = title.trim().ifBlank { return null }
        return Award(
            title = normalizedTitle,
            organization = organization.trim().ifBlank { null },
            awardedAt = awardedAt.toLocalDate(),
        )
    }

}

data class ExtractedCertification(
    val name: String = "",
    val issuer: String = "",
    val acquiredAt: ExtractedDate = ExtractedDate(),
) {

    fun toCertification(): Certification? {
        val normalizedName = name.trim().ifBlank { return null }
        return Certification(
            name = normalizedName,
            issuer = issuer.trim().ifBlank { null },
            acquiredAt = acquiredAt.toLocalDate(),
        )
    }

}

data class ExtractedSkill(
    val name: String = "",
    val level: String = "",
) {

    fun toSkill(): ProfileSkill? {
        val normalizedName = name.trim().ifBlank { return null }
        return ProfileSkill(
            name = normalizedName,
            level = level.toEnumOrNull<SkillLevel>(),
        )
    }

}

data class ExtractedDate(
    val year: Int? = null,
    val month: Int? = null,
) {

    // 원문에 연도만 있고 월이 없으면 날짜를 만들지 않는다 (없는 정보를 지어내지 않음)
    fun toLocalDate(): LocalDate? {
        val year = year ?: return null
        val month = month?.takeIf { it in 1..12 } ?: return null
        return runCatching { LocalDate.of(year.normalizedYear(), month, 1) }.getOrNull()
    }

}

private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? {
    val normalized = trim().uppercase()
    return enumValues<T>().firstOrNull { it.name == normalized }
}

data class ExtractedExperienceProject(
    val name: String = "",
    val summary: String = "",
    val period: ExtractedPeriod = ExtractedPeriod(),
    val periodText: String = "",
    val role: String = "",
    val company: String = "",
    val experiences: List<ExtractedExperience> = emptyList(),
) {

    fun toCommandGroup(): ImportedExperienceCommandGroup? {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            return null
        }

        val projectPeriod = period.toPeriod() ?: PeriodParser.parse(periodText)
        val commands = experiences.mapNotNull { experience -> experience.toCommand(projectPeriod, role) }
        if (commands.isEmpty()) {
            return null
        }

        return ImportedExperienceCommandGroup(
            project = ExperienceProjectCreateCommand(
                name = normalizedName,
                summary = summary.trim().ifBlank { normalizedName },
                period = period.toPeriod() ?: PeriodParser.parse(periodText),
                role = role.trim().ifBlank { null },
            ),
            experiences = commands,
        )
    }

}

data class ExtractedExperience(
    val title: String = "",
    val period: ExtractedPeriod = ExtractedPeriod(),
    val periodText: String = "",
    val role: String = "",
    val situation: String = "",
    val task: String = "",
    val action: String = "",
    val result: String = "",
    val competencyTags: List<String> = emptyList(),
) {

    fun toCommand(projectPeriod: Period?, projectRole: String): ExperienceCreateCommand? {
        val normalizedTitle = title.trim().ifBlank { action.trim() }.ifBlank { result.trim() }
        if (normalizedTitle.isBlank()) {
            return null
        }

        if (listOf(situation, task, action, result).all { value -> value.isBlank() }) {
            return null
        }

        return ExperienceCreateCommand(
            tags = competencyTags.map { tag -> tag.trim() }.filter { tag -> tag.isNotBlank() }.distinct(),
            title = normalizedTitle,
            contents = ExperienceContents.star(
                situation = situation.trim(),
                task = task.trim(),
                action = action.trim(),
                result = result.trim(),
            ),
            period = period.toPeriod() ?: PeriodParser.parse(periodText) ?: projectPeriod,
            role = role.trim().ifBlank { projectRole.trim() }.ifBlank { null },
        )
    }

}

data class ExtractedPeriod(
    val startYear: Int? = null,
    val startMonth: Int? = null,
    val endYear: Int? = null,
    val endMonth: Int? = null,
    val isCurrent: Boolean = false,
) {

    fun toPeriod(): Period? {
        val startYear = startYear ?: return null
        val startMonth = startMonth?.takeIf { month -> month in 1..12 } ?: return null
        val startAt = runCatching { LocalDate.of(startYear.normalizedYear(), startMonth, 1) }.getOrNull()
            ?: return null

        val endAt = if (isCurrent) {
            null
        } else {
            val endYear = endYear ?: return Period(startAt = startAt, endAt = null)
            val endMonth = endMonth?.takeIf { month -> month in 1..12 }
                ?: return Period(startAt = startAt, endAt = null)
            val candidateEndAt = runCatching {
                YearMonth.of(endYear.normalizedYear(), endMonth).atEndOfMonth()
            }.getOrNull()

            // Discard invalid or reversed end date
            if (candidateEndAt != null && candidateEndAt.isBefore(startAt)) null else candidateEndAt
        }

        return Period(startAt = startAt, endAt = endAt)
    }

}

private fun Int.normalizedYear(): Int {
    if (this >= 1000) {
        return this
    }

    return if (this >= 70) {
        1900 + this
    } else {
        2000 + this
    }
}

private object PeriodParser {
    private val yearMonthRegex = Regex("""(?<!\d)((?:20|19)?\d{2})[.\-/년\s]*(0?[1-9]|1[0-2])?""")
    private val ongoingRegex = Regex("""(현재|재직중|진행중|present|current|now)""", RegexOption.IGNORE_CASE)

    fun parse(value: String): Period? {
        val matches = yearMonthRegex.findAll(value).toList()
        if (matches.isEmpty()) {
            return null
        }

        val startAt = matches.first().toStartDate()
        val endAt = if (ongoingRegex.containsMatchIn(value)) {
            null
        } else {
            matches.getOrNull(1)?.toEndDate()
        }

        return runCatching { Period(startAt = startAt, endAt = endAt) }.getOrNull()
    }

    private fun MatchResult.toStartDate(): LocalDate {
        val year = groupValues[1].toYear()
        val month = groupValues[2].toIntOrNull() ?: 1
        return LocalDate.of(year, month, 1)
    }

    private fun MatchResult.toEndDate(): LocalDate {
        val year = groupValues[1].toYear()
        val month = groupValues[2].toIntOrNull() ?: 12
        return YearMonth.of(year, month).atEndOfMonth()
    }

    private fun String.toYear(): Int {
        if (length == 4) {
            return toInt()
        }

        val year = toInt()
        return if (year >= 70) {
            1900 + year
        } else {
            2000 + year
        }
    }
}
