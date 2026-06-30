package com.jobdori.core.domain.experience.service

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
        projectId: Long?,
        tags: List<String>?,
        title: String?,
        contents: ExperienceContents?,
    ): Experience {
        val experience = experienceRepository.findByIdAndWorkspaceId(experienceId, workspaceId)
            ?: throw ExperienceNotFoundException(
                "존재하지 않는 경험입니다. [workspaceId=$workspaceId, experienceId=$experienceId]",
            )
        val targetProjectId = projectId ?: experience.projectId

        return experienceRepository.save(
            experience.copy(
                projectId = targetProjectId,
                tags = tags ?: experience.tags,
                title = title ?: experience.title,
                contents = contents ?: experience.contents,
            ),
        )
    }

}
