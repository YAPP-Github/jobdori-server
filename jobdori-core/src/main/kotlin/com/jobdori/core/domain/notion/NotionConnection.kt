package com.jobdori.core.domain.notion

import java.time.LocalDateTime
import java.util.UUID

data class NotionConnection(
    val id: Long,
    val publicId: String,
    val workspaceId: Long,
    val notionWorkspaceId: String,
    val workspaceName: String?,
    val workspaceIcon: String?,
    val botId: String,
    val accessToken: String,
    val refreshToken: String,
    val lastRefreshedAt: LocalDateTime?,
) {

    fun refresh(accessToken: String, refreshToken: String, refreshedAt: LocalDateTime = LocalDateTime.now()) = copy(
        accessToken = accessToken,
        refreshToken = refreshToken,
        lastRefreshedAt = refreshedAt,
    )

    companion object {
        fun newInstance(
            workspaceId: Long,
            token: NotionOAuthToken,
        ) = NotionConnection(
            id = 0L,
            publicId = UUID.randomUUID().toString(),
            workspaceId = workspaceId,
            notionWorkspaceId = token.notionWorkspaceId,
            workspaceName = token.workspaceName,
            workspaceIcon = token.workspaceIcon,
            botId = token.botId,
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            lastRefreshedAt = null,
        )
    }

}
