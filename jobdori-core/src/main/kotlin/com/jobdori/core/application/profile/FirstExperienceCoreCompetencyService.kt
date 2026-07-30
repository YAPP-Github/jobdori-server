package com.jobdori.core.application.profile

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.profile.service.ProfileModifier
import com.jobdori.core.domain.profile.service.ProfileReader
import com.jobdori.core.domain.profile.service.command.ProfileUpdateCommand
import org.springframework.stereotype.Service

@Service
class FirstExperienceCoreCompetencyService(
    private val profileReader: ProfileReader,
    private val profileModifier: ProfileModifier,
    private val profileAiService: ProfileAiService,
) {

    fun generateIfAbsent(workspaceId: Long, experience: Experience) {
        generateIfAbsent(workspaceId, listOf(experience))
    }

    fun generateIfAbsent(workspaceId: Long, experiences: List<Experience>) {
        if (experiences.isEmpty()) return

        val profile = profileReader.getOrCreateProfile(workspaceId)
        if (!profile.coreCompetency.isNullOrBlank()) return

        val coreCompetency = profileAiService.generateCoreCompetencyFromExperiences(
            detail = profileReader.getDetail(profile),
            experiences = experiences,
        ).trim()
        if (coreCompetency.isBlank()) return

        profileModifier.modify(
            profile = profile,
            command = ProfileUpdateCommand(coreCompetency = coreCompetency),
        )
    }

}
