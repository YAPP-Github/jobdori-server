package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceProjectCreator(
    private val experienceProjectRepository: ExperienceProjectRepository,
) {

    @Transactional
    fun create(
        workspaceId: Long,
        name: String,
        summary: String,
        period: Period?,
        role: String?,
    ): ExperienceProject {
        return experienceProjectRepository.save(
            ExperienceProject.newInstance(
                workspaceId = workspaceId,
                name = name,
                summary = summary,
                period = period,
                role = role,
            ),
        )
    }

}
