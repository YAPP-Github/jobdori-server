package com.jobdori.core.domain.notion.service

import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.NotionOAuthToken
import com.jobdori.core.domain.notion.repository.NotionConnectionRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class NotionConnectionStoreTest : StringSpec({

    val connectionRepository = mockk<NotionConnectionRepository>()
    val connectionStore = NotionConnectionStore(connectionRepository)

    "새 Notion OAuth 토큰은 연결을 생성해서 저장한다" {
        // given
        val token = notionOAuthToken(accessToken = "new-access-token")
        val savedConnection = notionStoreConnection(id = 1L, workspaceId = 10L, accessToken = "new-access-token")
        val connectionSlot = slot<NotionConnection>()

        every {
            connectionRepository.findByWorkspaceIdAndNotionWorkspaceIdAndBotId(
                workspaceId = 10L,
                notionWorkspaceId = "notion-workspace-id",
                botId = "bot-id",
            )
        } returns null
        every { connectionRepository.save(capture(connectionSlot)) } returns savedConnection

        // when
        val result = connectionStore.saveConnectedToken(workspaceId = 10L, token = token)

        // then
        result shouldBe savedConnection
        connectionSlot.captured.id shouldBe 0L
        connectionSlot.captured.workspaceId shouldBe 10L
        connectionSlot.captured.accessToken shouldBe "new-access-token"
        connectionSlot.captured.refreshToken shouldBe "refresh-token"
        verify(exactly = 1) { connectionRepository.save(any()) }
    }

    "이미 연결된 Notion 워크스페이스와 봇이면 기존 연결 토큰을 갱신한다" {
        // given
        val existingConnection = notionStoreConnection(
            id = 5L,
            publicId = "existing-connection-id",
            workspaceId = 10L,
            workspaceName = "Old",
            accessToken = "old-access-token",
            refreshToken = "old-refresh-token",
        )
        val token = notionOAuthToken(
            accessToken = "updated-access-token",
            refreshToken = "updated-refresh-token",
            workspaceName = "Updated",
            workspaceIcon = "https://example.com/updated.png",
        )
        val connectionSlot = slot<NotionConnection>()

        every {
            connectionRepository.findByWorkspaceIdAndNotionWorkspaceIdAndBotId(
                workspaceId = 10L,
                notionWorkspaceId = "notion-workspace-id",
                botId = "bot-id",
            )
        } returns existingConnection
        every { connectionRepository.save(capture(connectionSlot)) } answers { connectionSlot.captured }

        // when
        val result = connectionStore.saveConnectedToken(workspaceId = 10L, token = token)

        // then
        result.id shouldBe 5L
        result.publicId shouldBe "existing-connection-id"
        result.workspaceName shouldBe "Updated"
        result.workspaceIcon shouldBe "https://example.com/updated.png"
        result.accessToken shouldBe "updated-access-token"
        result.refreshToken shouldBe "updated-refresh-token"
    }

})

private fun notionOAuthToken(
    accessToken: String = "access-token",
    refreshToken: String = "refresh-token",
    workspaceName: String? = "Jobdori",
    workspaceIcon: String? = "https://example.com/icon.png",
) = NotionOAuthToken(
    accessToken = accessToken,
    refreshToken = refreshToken,
    botId = "bot-id",
    notionWorkspaceId = "notion-workspace-id",
    workspaceName = workspaceName,
    workspaceIcon = workspaceIcon,
)

private fun notionStoreConnection(
    id: Long = 1L,
    publicId: String = "connection-$id",
    workspaceId: Long = 1L,
    workspaceName: String? = "Jobdori",
    workspaceIcon: String? = "https://example.com/icon.png",
    accessToken: String = "access-token",
    refreshToken: String = "refresh-token",
) = NotionConnection(
    id = id,
    publicId = publicId,
    workspaceId = workspaceId,
    notionWorkspaceId = "notion-workspace-id",
    workspaceName = workspaceName,
    workspaceIcon = workspaceIcon,
    botId = "bot-id",
    accessToken = accessToken,
    refreshToken = refreshToken,
    lastRefreshedAt = null,
)
