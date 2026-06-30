package com.jobdori.api.application.workspace.service

import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.error.WorkspaceAccessDeniedException
import com.jobdori.core.domain.workspace.service.WorkspaceReader
import org.springframework.stereotype.Service

@Service
class WorkspaceAccessValidationService(
    private val workspaceReader: WorkspaceReader,
) {

    fun validateAccessible(workspaceId: String, userId: Long): Workspace {
        val workspace = workspaceReader.getWorkspace(workspaceId)

        if (!workspace.isOwner(userId)) {
            throw WorkspaceAccessDeniedException("워크스페이스 접근 권한이 없습니다 [workspaceId=$workspaceId, userId=$userId]")
        }

        return workspace
    }

}
