package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.error.ExperienceNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceModifier(
    private val experienceRepository: ExperienceRepository,
) {

    @Transactional
    fun modify(
        workspaceId: Long,
        experienceId: Long,
        projectId: Long,
        tags: List<String>,
        title: String,
        contents: ExperienceContents,
        period: Period?,
        role: String?,
    ): Experience {
        val experience = experienceRepository.findByIdAndWorkspaceId(experienceId, workspaceId)
            ?: throw ExperienceNotFoundException(
                "존재하지 않는 경험입니다. [workspaceId=$workspaceId, experienceId=$experienceId]",
            )
        return experienceRepository.save(
            experience.copy(
                projectId = projectId,
                tags = tags,
                title = title,
                contents = contents,
                period = period,
                role = role,
            ),
        )
    }

}
