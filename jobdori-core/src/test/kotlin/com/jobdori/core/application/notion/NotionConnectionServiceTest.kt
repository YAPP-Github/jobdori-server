package com.jobdori.core.application.notion

import com.jobdori.core.application.notion.client.NotionOAuthTokenClient
import com.jobdori.core.application.notion.command.ConnectNotionCommand
import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.NotionOAuthToken
import com.jobdori.core.domain.notion.service.NotionConnectionReader
import com.jobdori.core.domain.notion.service.NotionConnectionStore
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NotionConnectionServiceTest : StringSpec({

    val tokenClient = mockk<NotionOAuthTokenClient>()
    val connectionReader = mockk<NotionConnectionReader>()
    val connectionStore = mockk<NotionConnectionStore>()
    val connectionService = NotionConnectionService(
        tokenClient = tokenClient,
        connectionReader = connectionReader,
        connectionStore = connectionStore,
    )

    "인가 코드를 토큰으로 교환한 뒤 Notion 연결을 저장한다" {
        // given
        val token = NotionOAuthToken(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            botId = "bot-id",
            notionWorkspaceId = "notion-workspace-id",
            workspaceName = "Jobdori",
            workspaceIcon = null,
        )
        val connection = notionConnectionServiceConnection(id = 1L, workspaceId = 10L)
        every {
            tokenClient.exchangeAuthorizationCode(
                authorizationCode = "authorization-code",
                redirectUri = "https://example.com/oauth/callback",
            )
        } returns token
        every {
            connectionStore.saveConnectedToken(workspaceId = 10L, token = token)
        } returns connection

        // when
        val result = connectionService.connect(
            ConnectNotionCommand(
                workspaceId = 10L,
                authorizationCode = "authorization-code",
                redirectUri = "https://example.com/oauth/callback",
            )
        )

        // then
        result shouldBe connection
        verify(exactly = 1) { connectionStore.saveConnectedToken(workspaceId = 10L, token = token) }
    }

    "목록 조회 size를 1에서 30 사이로 보정한다" {
        // given
        every {
            connectionReader.findAllByWorkspaceId(
                workspaceId = 10L,
                cursor = null,
                size = 30,
            )
        } returns com.jobdori.common.model.SliceResult(
            items = emptyList(),
            nextCursor = null,
        )

        // when
        connectionService.list(workspaceId = 10L, cursor = null, size = 100)

        // then
        verify(exactly = 1) {
            connectionReader.findAllByWorkspaceId(
                workspaceId = 10L,
                cursor = null,
                size = 30,
            )
        }
    }

})

private fun notionConnectionServiceConnection(
    id: Long = 1L,
    workspaceId: Long = 1L,
) = NotionConnection(
    id = id,
    workspaceId = workspaceId,
    notionWorkspaceId = "notion-workspace-id",
    workspaceName = "Jobdori",
    workspaceIcon = null,
    botId = "bot-id",
    accessToken = "access-token",
    refreshToken = "refresh-token",
    lastRefreshedAt = null,
)
