package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.error.ExperienceProjectNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceProjectModifier(
    private val experienceProjectRepository: ExperienceProjectRepository,
) {

    @Transactional
    fun modify(
        workspaceId: Long,
        projectId: Long,
        name: String,
        summary: String,
        period: Period?,
        role: String?,
    ): ExperienceProject {
        val project = experienceProjectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
            ?: throw ExperienceProjectNotFoundException(
                "존재하지 않는 경험 프로젝트입니다. [workspaceId=$workspaceId, projectId=$projectId]",
            )

        return experienceProjectRepository.save(
            project.copy(
                name = name,
                summary = summary,
                period = period,
                role = role,
            ),
        )
    }

}
