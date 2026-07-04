package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceEntity
import org.springframework.data.domain.Pageable

interface ExperienceCustomRepository {

    fun searchAllByWorkspaceIdAndStatus(
        workspaceId: Long,
        status: ExperienceStatus,
        keywordPattern: String,
        cursorId: Long?,
        pageable: Pageable,
    ): List<ExperienceEntity>

}
