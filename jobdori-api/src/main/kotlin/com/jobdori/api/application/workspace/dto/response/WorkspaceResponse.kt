package com.jobdori.api.application.workspace.dto.response

import com.jobdori.core.domain.workspace.Workspace

data class WorkspaceResponse(
    val workspaceId: String,
) {

    companion object {
        fun from(workspace: Workspace) = WorkspaceResponse(
            workspaceId = workspace.publicId,
        )
    }

}
