package com.jobdori.api.application.experience.service

import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.experience.ExperienceAiExtractionService
import com.jobdori.core.application.experience.ExperienceImportService
import com.jobdori.core.application.profile.FirstExperienceCoreCompetencyService
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.profile.service.ProfileModifier
import com.jobdori.core.domain.profile.service.ProfileReader
import org.springframework.stereotype.Service

@Service
class ExperienceTextImportService(
    private val experienceImportService: ExperienceImportService,
    private val experienceAiExtractionService: ExperienceAiExtractionService,
    private val profileReader: ProfileReader,
    private val profileModifier: ProfileModifier,
    private val experienceReader: ExperienceReader,
    private val firstExperienceCoreCompetencyService: FirstExperienceCoreCompetencyService,
) {

    fun import(workspaceId: Long, text: String) {
        val result = experienceAiExtractionService.extract(text)
        val isFirstImport = experienceReader.findAllActive(workspaceId).isEmpty()

        experienceImportService.saveAll(
            workspaceId = workspaceId,
            groups = result.toCommandGroups(),
        )

        val profile = profileReader.getOrCreateProfile(workspaceId)
        profileModifier.modify(profile, result.toProfileUpdateCommand(profileReader.getDetail(profile)))

        if (isFirstImport) {
            runCatching {
                firstExperienceCoreCompetencyService.generateIfAbsent(
                    workspaceId = workspaceId,
                    experiences = experienceReader.findAllActive(workspaceId),
                )
            }.onFailure { e ->
                log.warn(e) { "이력서 임포트 핵심역량 생성 실패, 임포트는 유지: workspaceId=$workspaceId" }
            }
        }
    }

}
