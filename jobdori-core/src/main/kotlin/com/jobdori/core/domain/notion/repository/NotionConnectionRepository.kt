package com.jobdori.core.domain.notion.repository

import com.jobdori.core.domain.notion.NotionConnection

interface NotionConnectionRepository {

    fun save(connection: NotionConnection): NotionConnection

    fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): NotionConnection?

    fun findAllByWorkspaceId(workspaceId: Long, cursorId: Long?, size: Int): List<NotionConnection>

    fun findByWorkspaceIdAndNotionWorkspaceIdAndBotId(
        workspaceId: Long,
        notionWorkspaceId: String,
        botId: String,
    ): NotionConnection?

    fun deleteByIdAndWorkspaceId(id: Long, workspaceId: Long)

}
