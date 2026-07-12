package com.jobdori.core.domain.workspace.service

import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.repository.WorkspaceRepository
import org.springframework.stereotype.Service

@Service
class WorkspaceCreator(
    private val workspaceRepository: WorkspaceRepository,
) {

    fun create(ownerUserId: Long): Workspace {
        return workspaceRepository.save(
            Workspace.newInstance(
                ownerUserId = ownerUserId,
            ),
        )
    }

}
