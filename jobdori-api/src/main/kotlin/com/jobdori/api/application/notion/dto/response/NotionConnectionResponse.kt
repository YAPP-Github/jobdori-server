package com.jobdori.api.application.notion.dto.response

import com.jobdori.core.domain.notion.NotionConnection

data class NotionConnectionResponse(
    val connectionId: String,
    val notionWorkspaceName: String?,
    val notionWorkspaceIcon: String?,
) {

    companion object {
        fun from(connection: NotionConnection) = NotionConnectionResponse(
            connectionId = connection.publicId,
            notionWorkspaceName = connection.workspaceName,
            notionWorkspaceIcon = connection.workspaceIcon,
        )
    }

}
