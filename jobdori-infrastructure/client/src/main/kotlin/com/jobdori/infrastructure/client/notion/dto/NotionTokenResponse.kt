package com.jobdori.infrastructure.client.notion.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NotionTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("refresh_token")
    val refreshToken: String,

    @JsonProperty("bot_id")
    val botId: String,

    @JsonProperty("workspace_id")
    val workspaceId: String,

    @JsonProperty("workspace_name")
    val workspaceName: String? = null,

    @JsonProperty("workspace_icon")
    val workspaceIcon: String? = null,
)
