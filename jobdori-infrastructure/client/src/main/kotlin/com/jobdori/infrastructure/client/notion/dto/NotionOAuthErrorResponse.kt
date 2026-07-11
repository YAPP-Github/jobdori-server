package com.jobdori.infrastructure.client.notion.dto

data class NotionOAuthErrorResponse(
    val error: String? = null,
    val message: String? = null,
)
