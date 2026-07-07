package com.jobdori.api.application.notion.dto.response

import com.jobdori.core.domain.notion.NotionConnection

data class NotionConnectionResponse(
    val publicId: String,
    val notionWorkspaceId: String,
    val workspaceName: String?,
    val workspaceIcon: String?,
    val botId: String,
) {

    companion object {
        fun from(connection: NotionConnection) = NotionConnectionResponse(
            publicId = connection.publicId,
            notionWorkspaceId = connection.notionWorkspaceId,
            workspaceName = connection.workspaceName,
            workspaceIcon = connection.workspaceIcon,
            botId = connection.botId,
        )
    }

}
