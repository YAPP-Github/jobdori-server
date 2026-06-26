package com.jobdori.core.domain.workspace.repository

import com.jobdori.core.domain.workspace.Workspace

interface WorkspaceRepository {

    fun findByPublicId(publicId: String): Workspace?

    fun findAllByOwnerUserId(ownerUserId: Long): List<Workspace>

    fun save(workspace: Workspace): Workspace

}
