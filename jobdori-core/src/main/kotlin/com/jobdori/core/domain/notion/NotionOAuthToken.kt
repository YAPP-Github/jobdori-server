package com.jobdori.core.domain.notion

data class NotionOAuthToken(
    val accessToken: String,
    val refreshToken: String,
    val botId: String,
    val notionWorkspaceId: String,
    val workspaceName: String?,
    val workspaceIcon: String?,
)
