package com.jobdori.api.application.user.controller

import com.jobdori.api.application.user.dto.response.UserGraphQlResponse
import com.jobdori.api.application.workspace.dto.response.WorkspaceResponse
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.domain.user.service.UserReader
import com.jobdori.core.domain.workspace.service.WorkspaceReader
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class UserQueryResolver(
    private val userReader: UserReader,
    private val workspaceReader: WorkspaceReader,
) {

    @QueryMapping
    fun me(
        @UserId userId: Long,
    ): UserGraphQlResponse {
        val user = userReader.getUser(userId)
        return UserGraphQlResponse.from(user)
    }

    @SchemaMapping(typeName = "User", field = "workspaces")
    fun workspaces(user: UserGraphQlResponse): List<WorkspaceResponse> {
        return workspaceReader.getWorkspaces(ownerUserId = user.id)
            .map { WorkspaceResponse.from(it) }
    }

}
