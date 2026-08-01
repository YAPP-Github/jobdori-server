package com.jobdori.core.application.profile

import com.jobdori.common.logger.LoggerExtension.log
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

    fun generateIfAbsent(workspaceId: Long, experiences: List<Experience>) {
        val profile = profileReader.getOrCreateProfile(workspaceId)
        if (!profile.coreCompetency.isNullOrBlank()) return

        val coreCompetency = profileAiService.generateCoreCompetencyFromExperiences(
            detail = profileReader.getDetail(profile),
            experiences = experiences,
        ).trim()
        // AI가 200에 빈 content를 주면 ai_call은 success=true로 남아 실패가 관측되지 않는다
        if (coreCompetency.isBlank()) {
            log.warn { "핵심역량 생성 결과가 비어 저장하지 않음: workspaceId=$workspaceId" }
            return
        }

        profileModifier.modify(
            profile = profile,
            command = ProfileUpdateCommand(coreCompetency = coreCompetency),
        )
    }

}
