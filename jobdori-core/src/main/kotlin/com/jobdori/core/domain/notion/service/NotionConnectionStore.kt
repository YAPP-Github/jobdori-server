package com.jobdori.core.domain.notion.service

import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.NotionOAuthToken
import com.jobdori.core.domain.notion.repository.NotionConnectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotionConnectionStore(
    private val connectionRepository: NotionConnectionRepository,
) {

    fun save(connection: NotionConnection): NotionConnection {
        return connectionRepository.save(connection)
    }

    @Transactional
    fun saveConnectedToken(workspaceId: Long, token: NotionOAuthToken): NotionConnection {
        val existingConnection = connectionRepository.findByWorkspaceIdAndNotionWorkspaceIdAndBotId(
            workspaceId = workspaceId,
            notionWorkspaceId = token.notionWorkspaceId,
            botId = token.botId,
        )
        val connection = existingConnection?.copy(
            workspaceName = token.workspaceName,
            workspaceIcon = token.workspaceIcon,
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
        ) ?: NotionConnection.newInstance(
            workspaceId = workspaceId,
            token = token,
        )

        return connectionRepository.save(connection)
    }

    fun deleteByPublicIdAndWorkspaceId(publicId: String, workspaceId: Long) {
        connectionRepository.deleteByPublicIdAndWorkspaceId(publicId, workspaceId)
    }

}
