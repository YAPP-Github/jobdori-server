package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceCreator(
    private val experienceRepository: ExperienceRepository,
) {

    @Transactional
    fun create(
        workspaceId: Long,
        projectId: Long,
        tags: List<String>,
        title: String,
        contents: ExperienceContents,
    ): Experience {
        return experienceRepository.save(
            Experience.newInstance(
                workspaceId = workspaceId,
                projectId = projectId,
                tags = tags,
                title = title,
                contents = contents,
            ),
        )
    }

}
