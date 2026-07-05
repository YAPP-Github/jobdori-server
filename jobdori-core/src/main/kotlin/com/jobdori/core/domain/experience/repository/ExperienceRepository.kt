package com.jobdori.core.domain.experience.repository

import com.jobdori.core.domain.experience.Experience

interface ExperienceRepository {

    fun save(experience: Experience): Experience

    fun saveAll(experiences: List<Experience>): List<Experience>

    fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): Experience?

    fun findAllByWorkspaceId(workspaceId: Long, cursorId: Long?, size: Int): List<Experience>

    fun findAllByWorkspaceIdAndProjectId(
        workspaceId: Long,
        projectId: Long,
        cursorId: Long?,
        size: Int,
    ): List<Experience>

    fun searchAllByWorkspaceId(
        workspaceId: Long,
        keyword: String,
        cursorId: Long?,
        size: Int,
    ): List<Experience>

    fun countByWorkspaceIdAndProjectIds(workspaceId: Long, projectIds: Collection<Long>): Map<Long, Long>

}
