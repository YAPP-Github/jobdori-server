package com.jobdori.core.domain.notion

data class NotionBlock(
    val id: String,
    val type: String,
    val plainText: String,
    val children: List<NotionBlock> = emptyList(),
)
