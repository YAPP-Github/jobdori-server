package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.error.ExperienceProjectNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExperienceProjectRemover(
    private val experienceProjectRepository: ExperienceProjectRepository,
) {

    @Transactional
    fun remove(workspaceId: Long, projectId: Long) {
        val project = experienceProjectRepository.findByIdAndWorkspaceId(projectId, workspaceId)
            ?: throw ExperienceProjectNotFoundException(
                "존재하지 않는 경험 프로젝트입니다. [workspaceId=$workspaceId, projectId=$projectId]",
            )
        experienceProjectRepository.save(project.copy(status = ExperienceProjectStatus.DELETED))
    }

}
