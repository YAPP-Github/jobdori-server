package com.jobdori.api.application.notion.controller

import com.jobdori.api.application.notion.dto.response.NotionConnectionListResponse
import com.jobdori.api.application.notion.dto.response.NotionPageSummaryListResponse
import com.jobdori.api.application.notion.service.NotionConnectionApiService
import com.jobdori.api.support.auth.UserId
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class NotionQueryResolver(
    private val notionConnectionApiService: NotionConnectionApiService,
) {

    @QueryMapping
    fun notionConnections(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument cursor: String?,
        @Argument size: Int,
    ): NotionConnectionListResponse = notionConnectionApiService.listConnections(
        userId = userId,
        workspaceId = workspaceId,
        cursor = cursor,
        size = size,
    )

    @QueryMapping
    fun notionPages(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument connectionId: String,
        @Argument query: String?,
        @Argument cursor: String?,
        @Argument pageSize: Int,
    ): NotionPageSummaryListResponse = notionConnectionApiService.searchPages(
        userId = userId,
        workspaceId = workspaceId,
        connectionId = connectionId,
        query = query,
        cursor = cursor,
        pageSize = pageSize,
    )

}
