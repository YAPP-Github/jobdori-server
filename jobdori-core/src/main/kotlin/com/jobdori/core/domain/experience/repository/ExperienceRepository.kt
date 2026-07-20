package com.jobdori.core.domain.experience.repository

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceStatus

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

    fun updateStatusByWorkspaceIdAndProjectId(
        workspaceId: Long,
        projectId: Long,
        status: ExperienceStatus,
    )

    // 워크스페이스의 ACTIVE 경험 전체(비페이지네이션). AI 추천 입력용.
    fun findAllActiveByWorkspaceId(workspaceId: Long): List<Experience>

    // 경험 세트 변경 감지용 시그니처("$count:$maxUpdatedAt"). 추가/수정/삭제가 모두 반영된다.
    fun experienceSignature(workspaceId: Long): String

}
