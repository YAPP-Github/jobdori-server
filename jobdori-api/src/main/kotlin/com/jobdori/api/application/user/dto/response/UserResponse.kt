package com.jobdori.api.application.user.dto.response

import com.jobdori.api.application.workspace.dto.response.WorkspaceResponse
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.workspace.Workspace

data class UserResponse(
    val userId: String,
    val name: String,
    val profileImageUrl: String?,
    val workspaces: List<WorkspaceResponse>,
) {

    companion object {
        fun from(
            user: User,
            workspaces: List<Workspace>,
        ) = UserResponse(
            userId = user.publicId,
            name = user.name,
            profileImageUrl = user.profileImageUrl,
            workspaces = workspaces.map { WorkspaceResponse.from(it) },
        )
    }

}
