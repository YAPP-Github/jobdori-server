package com.jobdori.core.domain.notion.service

import com.jobdori.common.model.SliceResult
import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.error.NotionConnectionNotFoundException
import com.jobdori.core.domain.notion.repository.NotionConnectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotionConnectionReader(
    private val connectionRepository: NotionConnectionRepository,
) {

    @Transactional(readOnly = true)
    fun getByPublicIdAndWorkspaceId(publicId: String, workspaceId: Long): NotionConnection {
        return connectionRepository.findByPublicIdAndWorkspaceId(publicId, workspaceId)
            ?: throw NotionConnectionNotFoundException(
                message = "Notion 연결을 찾을 수 없습니다. [workspaceId=$workspaceId, connectionPublicId=$publicId]",
            )
    }

    @Transactional(readOnly = true)
    fun findAllByWorkspaceId(workspaceId: Long, cursor: String?, size: Int): SliceResult<NotionConnection> {
        val connections = connectionRepository.findAllByWorkspaceId(
            workspaceId = workspaceId,
            cursorId = cursor?.toLongOrNull(),
            size = size + 1,
        )
        val page = connections.take(size)
        return SliceResult(
            items = page,
            nextCursor = if (connections.size > size) page.lastOrNull()?.id?.toString() else null,
        )
    }

}
