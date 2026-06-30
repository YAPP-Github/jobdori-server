package com.jobdori.api.application.user.service

import com.jobdori.api.application.user.dto.response.UserResponse
import com.jobdori.api.application.user.dto.response.UserGraphQlResponse
import com.jobdori.api.application.workspace.dto.response.WorkspaceResponse
import com.jobdori.core.domain.user.service.UserReader
import com.jobdori.core.domain.workspace.service.WorkspaceReader
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userReader: UserReader,
    private val workspaceReader: WorkspaceReader,
) {

    fun getMyUser(userId: Long): UserResponse {
        val user = userReader.getUser(userId)
        val workspaces = workspaceReader.getWorkspaces(ownerUserId = user.id)

        return UserResponse.from(user, workspaces)
    }

    fun getMe(userId: Long): UserGraphQlResponse {
        val user = userReader.getUser(userId)

        return UserGraphQlResponse.from(user)
    }

    fun getWorkspaces(userId: Long): List<WorkspaceResponse> {
        return workspaceReader.getWorkspaces(ownerUserId = userId)
            .map { WorkspaceResponse.from(it) }
    }

}
