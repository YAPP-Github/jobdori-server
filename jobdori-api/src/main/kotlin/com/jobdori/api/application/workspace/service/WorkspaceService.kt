package com.jobdori.api.application.workspace.service

import com.jobdori.api.application.workspace.dto.response.WorkspaceResponse
import com.jobdori.core.domain.workspace.service.WorkspaceReader
import org.springframework.stereotype.Service

@Service
class WorkspaceService(
    private val workspaceReader: WorkspaceReader,
) {

    fun getWorkspaces(userId: Long): List<WorkspaceResponse> {
        return workspaceReader.getWorkspaces(ownerUserId = userId)
            .map { WorkspaceResponse.from(it) }
    }

}
