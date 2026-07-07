package com.jobdori.api.application.notion.controller

import com.jobdori.api.application.notion.dto.request.ListNotionConnectionRequest
import com.jobdori.api.application.notion.dto.request.ListNotionPageListRequest
import com.jobdori.api.application.notion.dto.response.NotionConnectionListResponse
import com.jobdori.api.application.notion.dto.response.NotionPageSummaryListResponse
import com.jobdori.api.application.notion.service.NotionConnectionApiService
import com.jobdori.api.support.auth.UserId
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.Arguments
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
        @Valid @Arguments request: ListNotionConnectionRequest,
    ): NotionConnectionListResponse = notionConnectionApiService.listConnections(
        userId = userId,
        workspaceId = workspaceId,
        cursor = request.cursor,
        size = request.size,
    )

    @QueryMapping
    fun notionPages(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument connectionId: String,
        @Argument query: String?,
        @Valid @Arguments request: ListNotionPageListRequest,
    ): NotionPageSummaryListResponse = notionConnectionApiService.searchPages(
        userId = userId,
        workspaceId = workspaceId,
        connectionId = connectionId,
        query = query,
        cursor = request.cursor,
        pageSize = request.size,
    )

}
