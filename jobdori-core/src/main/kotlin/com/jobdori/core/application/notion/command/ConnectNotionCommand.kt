package com.jobdori.core.application.notion.command

data class ConnectNotionCommand(
    val workspaceId: Long,
    val authorizationCode: String,
    val redirectUri: String,
)
