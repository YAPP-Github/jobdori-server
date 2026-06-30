package com.jobdori.api.application.user.controller

import com.jobdori.api.application.user.dto.response.UserGraphQlResponse
import com.jobdori.api.application.user.service.UserService
import com.jobdori.api.application.workspace.dto.response.WorkspaceResponse
import com.jobdori.api.application.workspace.service.WorkspaceService
import com.jobdori.api.support.auth.UserId
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class UserQueryResolver(
    private val userService: UserService,
    private val workspaceService: WorkspaceService,
) {

    @QueryMapping
    fun me(
        @UserId userId: Long,
    ): UserGraphQlResponse = userService.getMe(userId)

    @SchemaMapping(typeName = "User", field = "workspaces")
    fun workspaces(user: UserGraphQlResponse): List<WorkspaceResponse> {
        return workspaceService.getWorkspaces(userId = user.id)
    }

}
