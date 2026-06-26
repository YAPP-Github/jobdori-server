package com.jobdori.core.domain.workspace

import java.util.UUID

data class Workspace(
    val id: Long,
    val publicId: String,
    val ownerUserId: Long,
) {

    fun isOwner(userId: Long): Boolean = this.ownerUserId == userId

    companion object {
        fun newInstance(
            publicId: String = UUID.randomUUID().toString(),
            ownerUserId: Long,
        ) = Workspace(
            id = 0L,
            publicId = publicId,
            ownerUserId = ownerUserId,
        )
    }

}
