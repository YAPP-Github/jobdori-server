package com.jobdori.core.application.notion

import com.jobdori.common.model.SliceResult
import com.jobdori.core.application.notion.client.NotionOAuthTokenClient
import com.jobdori.core.application.notion.command.ConnectNotionCommand
import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.service.NotionConnectionReader
import com.jobdori.core.domain.notion.service.NotionConnectionStore
import org.springframework.stereotype.Service

@Service
class NotionConnectionService(
    private val tokenClient: NotionOAuthTokenClient,
    private val connectionReader: NotionConnectionReader,
    private val connectionStore: NotionConnectionStore,
) {

    fun connect(command: ConnectNotionCommand): NotionConnection {
        val token = tokenClient.exchangeAuthorizationCode(
            authorizationCode = command.authorizationCode,
            redirectUri = command.redirectUri,
        )
        return connectionStore.saveConnectedToken(
            workspaceId = command.workspaceId,
            token = token,
        )
    }

    fun list(workspaceId: Long, cursor: String?, size: Int): SliceResult<NotionConnection> {
        return connectionReader.findAllByWorkspaceId(
            workspaceId = workspaceId,
            cursor = cursor,
            size = size.coerceIn(1, 30),
        )
    }

    fun disconnect(workspaceId: Long, connectionPublicId: String) {
        connectionStore.deleteByPublicIdAndWorkspaceId(connectionPublicId, workspaceId)
    }

}
