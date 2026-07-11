package com.jobdori.api.application.notion.service

import com.jobdori.api.application.notion.dto.request.ConnectNotionRequest
import com.jobdori.api.application.notion.dto.response.NotionConnectionListResponse
import com.jobdori.api.application.notion.dto.response.NotionConnectionResponse
import com.jobdori.api.application.notion.dto.response.NotionPageSummaryListResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.core.application.notion.NotionConnectionService
import com.jobdori.core.application.notion.NotionPageService
import org.springframework.stereotype.Service

@Service
class NotionConnectionApiService(
    private val notionConnectionService: NotionConnectionService,
    private val notionPageService: NotionPageService,
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
) {

    fun connect(
        userId: Long,
        workspaceId: String,
        request: ConnectNotionRequest,
    ): NotionConnectionResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        return NotionConnectionResponse.from(notionConnectionService.connect(request.toCommand(workspace.id)))
    }

    fun listConnections(
        userId: Long,
        workspaceId: String,
        cursor: String?,
        size: Int,
    ): NotionConnectionListResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        return NotionConnectionListResponse.from(
            notionConnectionService.list(
                workspaceId = workspace.id,
                cursor = cursor,
                size = size,
            )
        )
    }

    fun disconnect(
        userId: Long,
        workspaceId: String,
        connectionId: String,
    ) {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        notionConnectionService.disconnect(workspace.id, connectionId)
    }

    fun searchPages(
        userId: Long,
        workspaceId: String,
        connectionId: String,
        query: String?,
        cursor: String?,
        pageSize: Int,
    ): NotionPageSummaryListResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        return NotionPageSummaryListResponse.from(
            notionPageService.searchPages(
                workspaceId = workspace.id,
                connectionPublicId = connectionId,
                query = query,
                startCursor = cursor,
                pageSize = pageSize,
            )
        )
    }

}
