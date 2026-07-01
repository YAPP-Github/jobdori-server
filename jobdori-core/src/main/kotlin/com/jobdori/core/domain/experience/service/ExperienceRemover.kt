package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.error.ExperienceNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceRemover(
    private val experienceRepository: ExperienceRepository,
) {

    @Transactional
    fun remove(workspaceId: Long, experienceId: Long) {
        val experience = experienceRepository.findByIdAndWorkspaceId(experienceId, workspaceId)
            ?: throw ExperienceNotFoundException(
                "존재하지 않는 경험입니다. [workspaceId=$workspaceId, experienceId=$experienceId]",
            )
        experienceRepository.save(experience.copy(status = ExperienceStatus.DELETED))
    }

}
