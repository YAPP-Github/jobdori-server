package com.jobdori.core.domain.workspace.service

import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkspaceCreator(
    private val workspaceRepository: WorkspaceRepository,
) {

    @Transactional
    fun create(ownerUserId: Long): Workspace {
        return workspaceRepository.save(
            Workspace.newInstance(
                ownerUserId = ownerUserId,
            ),
        )
    }

}
