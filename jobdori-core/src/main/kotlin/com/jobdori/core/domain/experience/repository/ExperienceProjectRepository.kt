package com.jobdori.core.domain.experience.repository

import com.jobdori.core.domain.experience.ExperienceProject

interface ExperienceProjectRepository {

    fun save(project: ExperienceProject): ExperienceProject

    fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): ExperienceProject?

    fun findAllByIdsAndWorkspaceId(ids: Collection<Long>, workspaceId: Long): List<ExperienceProject>

    fun findAllByWorkspaceId(workspaceId: Long, cursorId: Long?, size: Int): List<ExperienceProject>

}
