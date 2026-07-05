package com.jobdori.core.application.experience

import com.jobdori.common.model.Period
import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.experience.command.ImportedExperienceCommandGroup
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
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

    fun extract(pdfText: String): List<ImportedExperienceCommandGroup> {
        val prompt = promptTemplateRepository.findByType(PromptType.EXPERIENCE_STAR_EXTRACTION)
            ?: throw AiException(
                message = "경험 추출 프롬프트가 없습니다.",
                errorCode = AiErrorCode.E500_AI_GENERATION_FAILED,
            )

        val result = aiChatClient.generateStructured(
            prompt.buildStructured(
                userPrompt = buildUserPrompt(pdfText),
                responseType = ExperienceStarExtractionResult::class,
            ),
        )

        return result.toCommandGroups()
    }

    private fun buildUserPrompt(pdfText: String): String {
        return """
            다음 PDF 추출 텍스트에서 프로젝트와 경험을 추출해라.
            저장 가능한 프로젝트/경험만 반환하고, 원문에 없는 사실은 만들지 마라.
            프로젝트명 또는 경험 내용이 불명확하면 해당 항목은 제외해라.

            [PDF_TEXT]
            $pdfText
        """.trimIndent()
    }

}

data class ExperienceStarExtractionResult(
    val personalInfo: PersonalInfo = PersonalInfo(),
    val education: List<Education> = emptyList(),
    val certifications: List<String> = emptyList(),
    val projects: List<ExtractedExperienceProject> = emptyList(),
) {

    fun toCommandGroups(): List<ImportedExperienceCommandGroup> {
        return projects.mapNotNull { project -> project.toCommandGroup() }
    }

    data class PersonalInfo(
        val name: String = "",
        val phone: String = "",
        val email: String = "",
    )

    data class Education(
        val school: String = "",
        val degree: String = "",
        val period: String = "",
    )

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

        val commands = experiences.mapNotNull { experience -> experience.toCommand() }
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
    val situation: String = "",
    val task: String = "",
    val action: String = "",
    val result: String = "",
    val competencyTags: List<String> = emptyList(),
) {

    fun toCommand(): ExperienceCreateCommand? {
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
