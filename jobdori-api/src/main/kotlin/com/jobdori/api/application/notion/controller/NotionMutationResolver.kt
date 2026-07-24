package com.jobdori.api.application.notion.controller

import com.jobdori.api.application.notion.dto.request.ConnectNotionRequest
import com.jobdori.api.application.notion.dto.response.NotionConnectionResponse
import com.jobdori.api.application.notion.service.NotionConnectionApiService
import com.jobdori.api.support.auth.UserId
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class NotionMutationResolver(
    private val notionConnectionApiService: NotionConnectionApiService,
) {

    @MutationMapping
    fun connectNotion(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Valid @Argument request: ConnectNotionRequest,
    ): NotionConnectionResponse = notionConnectionApiService.connect(
        userId = userId,
        workspaceId = workspaceId,
        request = request,
    )

    @MutationMapping
    fun disconnectNotion(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument connectionId: Long,
    ): Boolean {
        notionConnectionApiService.disconnect(
            userId = userId,
            workspaceId = workspaceId,
            connectionId = connectionId,
        )
        return true
    }

}
