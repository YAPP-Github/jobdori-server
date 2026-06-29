package com.jobdori.core.application.workspace.service

import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.error.WorkspaceAccessDeniedException
import com.jobdori.core.domain.workspace.service.WorkspaceReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkspaceAccessValidationService(
    private val workspaceReader: WorkspaceReader,
) {

    @Transactional(readOnly = true)
    fun validateAccessible(workspacePublicId: String, userId: Long): Workspace {
        val workspace = workspaceReader.getWorkspace(workspacePublicId)

        if (!workspace.isOwner(userId)) {
            throw WorkspaceAccessDeniedException("워크스페이스 접근 권한이 없습니다 [workspacePublicId=$workspacePublicId, userId=$userId]")
        }

        return workspace
    }

}
