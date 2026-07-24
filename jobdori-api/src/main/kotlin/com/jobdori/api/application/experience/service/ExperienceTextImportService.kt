package com.jobdori.api.application.experience.service

import com.jobdori.core.application.experience.ExperienceAiExtractionService
import com.jobdori.core.application.experience.ExperienceImportService
import com.jobdori.core.domain.profile.service.ProfileModifier
import com.jobdori.core.domain.profile.service.ProfileReader
import org.springframework.stereotype.Service

@Service
class ExperienceTextImportService(
    private val experienceImportService: ExperienceImportService,
    private val experienceAiExtractionService: ExperienceAiExtractionService,
    private val profileReader: ProfileReader,
    private val profileModifier: ProfileModifier,
) {

    fun import(workspaceId: Long, text: String) {
        val result = experienceAiExtractionService.extract(text)

        experienceImportService.saveAll(
            workspaceId = workspaceId,
            groups = result.toCommandGroups(),
        )

        val profile = profileReader.getOrCreateProfile(workspaceId)
        profileModifier.modify(profile, result.toProfileUpdateCommand(profileReader.getDetail(profile)))
    }

}
