package com.jobdori.core.domain.workspace

import java.util.UUID

object WorkspaceFixture {

    fun create(
        id: Long = 0L,
        publicId: String = UUID.randomUUID().toString(),
        ownerUserId: Long = 1L,
    ) = Workspace(
        id = id,
        publicId = publicId,
        ownerUserId = ownerUserId,
    )

}
