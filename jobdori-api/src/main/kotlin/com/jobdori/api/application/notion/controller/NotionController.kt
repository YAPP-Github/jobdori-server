package com.jobdori.api.application.notion.controller

import com.jobdori.api.application.notion.dto.request.ConnectNotionRequest
import com.jobdori.api.application.notion.dto.response.NotionConnectionListResponse
import com.jobdori.api.application.notion.dto.response.NotionConnectionResponse
import com.jobdori.api.application.notion.dto.response.NotionPageSummaryListResponse
import com.jobdori.api.application.notion.service.NotionConnectionApiService
import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.rest.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class NotionController(
    private val notionConnectionApiService: NotionConnectionApiService,
) {

    @Authenticated
    @PostMapping("/v1/workspaces/{workspaceId}/notion/connections")
    fun connect(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @RequestBody @Valid request: ConnectNotionRequest,
    ): ApiResponse<NotionConnectionResponse> {
        return ApiResponse.ok(
            notionConnectionApiService.connect(
                userId = userId,
                workspaceId = workspaceId,
                request = request,
            )
        )
    }

    @Authenticated
    @GetMapping("/v1/workspaces/{workspaceId}/notion/connections")
    fun listConnections(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "10") size: Int,
    ): ApiResponse<NotionConnectionListResponse> {
        return ApiResponse.ok(
            notionConnectionApiService.listConnections(
                userId = userId,
                workspaceId = workspaceId,
                cursor = cursor,
                size = size,
            )
        )
    }

    @Authenticated
    @DeleteMapping("/v1/workspaces/{workspaceId}/notion/connections/{connectionId}")
    fun disconnect(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @PathVariable connectionId: String,
    ): ApiResponse<Nothing?> {
        notionConnectionApiService.disconnect(
            userId = userId,
            workspaceId = workspaceId,
            connectionId = connectionId,
        )
        return ApiResponse.OK
    }

    @Authenticated
    @GetMapping("/v1/workspaces/{workspaceId}/notion/connections/{connectionId}/pages")
    fun searchPages(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @PathVariable connectionId: String,
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ApiResponse<NotionPageSummaryListResponse> {
        return ApiResponse.ok(
            notionConnectionApiService.searchPages(
                userId = userId,
                workspaceId = workspaceId,
                connectionId = connectionId,
                query = query,
                cursor = cursor,
                pageSize = pageSize,
            )
        )
    }

}
