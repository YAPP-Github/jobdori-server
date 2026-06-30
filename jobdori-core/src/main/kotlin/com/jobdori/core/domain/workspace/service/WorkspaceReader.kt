package com.jobdori.core.domain.workspace.service

import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.error.WorkspaceNotFoundException
import com.jobdori.core.domain.workspace.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkspaceReader(
    private val workspaceRepository: WorkspaceRepository,
) {

    @Transactional(readOnly = true)
    fun getWorkspace(publicId: String): Workspace {
        return workspaceRepository.findByPublicId(publicId)
            ?: throw WorkspaceNotFoundException("등록되지 않은 워크스페이스 입니다. [workspaceId=$publicId]")
    }

    @Transactional(readOnly = true)
    fun getWorkspaces(ownerUserId: Long): List<Workspace> {
        return workspaceRepository.findAllByOwnerUserId(ownerUserId)
    }

}
