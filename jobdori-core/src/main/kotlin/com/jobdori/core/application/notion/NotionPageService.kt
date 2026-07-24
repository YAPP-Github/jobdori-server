package com.jobdori.core.application.notion

import com.jobdori.core.application.notion.client.NotionClient
import com.jobdori.core.application.notion.client.NotionOAuthTokenClient
import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.NotionPageContent
import com.jobdori.core.domain.notion.NotionPages
import com.jobdori.core.domain.notion.error.NotionConnectionNeedReconnectException
import com.jobdori.core.domain.notion.error.NotionUnauthorizedException
import com.jobdori.core.domain.notion.service.NotionConnectionReader
import com.jobdori.core.domain.notion.service.NotionConnectionStore
import org.springframework.stereotype.Service

@Service
class NotionPageService(
    private val notionClient: NotionClient,
    private val tokenClient: NotionOAuthTokenClient,
    private val connectionReader: NotionConnectionReader,
    private val connectionStore: NotionConnectionStore,
) {

    fun searchPages(
        workspaceId: Long,
        connectionId: Long,
        query: String?,
        startCursor: String?,
        pageSize: Int,
    ): NotionPages {
        val connection = getConnectedConnection(workspaceId, connectionId)
        return withTokenRefresh(connection) { activeConnection ->
            notionClient.searchPages(
                accessToken = activeConnection.accessToken,
                query = query,
                startCursor = startCursor,
                pageSize = pageSize.coerceIn(1, 100),
            )
        }
    }

    fun getPageContent(
        workspaceId: Long,
        connectionId: Long,
        pageId: String,
    ): NotionPageContent {
        val connection = getConnectedConnection(workspaceId, connectionId)
        return withTokenRefresh(connection) { activeConnection ->
            notionClient.getPageContent(
                accessToken = activeConnection.accessToken,
                pageId = pageId,
            )
        }
    }

    private fun getConnectedConnection(workspaceId: Long, connectionId: Long): NotionConnection {
        return connectionReader.getByIdAndWorkspaceId(
            id = connectionId,
            workspaceId = workspaceId,
        )
    }


    private fun <T> withTokenRefresh(
        connection: NotionConnection,
        block: (NotionConnection) -> T,
    ): T {
        return try {
            block(connection)
        } catch (_: NotionUnauthorizedException) {
            val refreshedConnection = refreshConnection(connection)
            try {
                block(refreshedConnection)
            } catch (retryException: NotionUnauthorizedException) {
                throw NotionConnectionNeedReconnectException(
                    message = "Notion 토큰 갱신 후에도 인증에 실패했습니다. ${connection.messageContext("retryNotionRequestAfterRefresh")}",
                    cause = retryException,
                )
            }
        }
    }

    private fun refreshConnection(connection: NotionConnection): NotionConnection {
        return try {
            val token = tokenClient.refresh(connection.refreshToken)
            connectionStore.save(
                connection.refresh(
                    accessToken = token.accessToken,
                    refreshToken = token.refreshToken,
                )
            )
        } catch (exception: NotionUnauthorizedException) {
            throw NotionConnectionNeedReconnectException(
                message = "Notion 토큰 갱신에 실패했습니다. ${connection.messageContext("refreshToken")}",
                cause = exception,
            )
        }
    }

    private fun NotionConnection.messageContext(operation: String): String {
        return "[operation=$operation, workspaceId=$workspaceId, connectionId=$id, notionWorkspaceId=$notionWorkspaceId, botId=$botId]"
    }

}
