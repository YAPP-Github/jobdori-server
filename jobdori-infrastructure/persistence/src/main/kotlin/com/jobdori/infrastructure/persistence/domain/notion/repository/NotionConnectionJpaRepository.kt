package com.jobdori.infrastructure.persistence.domain.notion.repository

import com.jobdori.infrastructure.persistence.domain.notion.entity.NotionConnectionEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface NotionConnectionJpaRepository : JpaRepository<NotionConnectionEntity, Long> {

    fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): NotionConnectionEntity?

    fun findAllByWorkspaceIdOrderByIdDesc(workspaceId: Long, pageable: Pageable): List<NotionConnectionEntity>

    fun findAllByWorkspaceIdAndIdLessThanOrderByIdDesc(
        workspaceId: Long,
        id: Long,
        pageable: Pageable,
    ): List<NotionConnectionEntity>

    fun findByWorkspaceIdAndNotionWorkspaceIdAndBotId(
        workspaceId: Long,
        notionWorkspaceId: String,
        botId: String,
    ): NotionConnectionEntity?

    fun deleteByIdAndWorkspaceId(id: Long, workspaceId: Long)

}
