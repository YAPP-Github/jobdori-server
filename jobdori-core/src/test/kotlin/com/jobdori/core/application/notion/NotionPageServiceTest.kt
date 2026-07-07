package com.jobdori.core.application.notion

import com.jobdori.core.application.notion.client.NotionClient
import com.jobdori.core.application.notion.client.NotionOAuthTokenClient
import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.NotionOAuthToken
import com.jobdori.core.domain.notion.NotionPageSummary
import com.jobdori.core.domain.notion.NotionPages
import com.jobdori.core.domain.notion.error.NotionConnectionNeedReconnectException
import com.jobdori.core.domain.notion.error.NotionUnauthorizedException
import com.jobdori.core.domain.notion.service.NotionConnectionReader
import com.jobdori.core.domain.notion.service.NotionConnectionStore
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NotionPageServiceTest : StringSpec({

    val notionClient = mockk<NotionClient>()
    val tokenClient = mockk<NotionOAuthTokenClient>()
    val connectionReader = mockk<NotionConnectionReader>()
    val connectionStore = mockk<NotionConnectionStore>()
    val notionPageService = NotionPageService(
        notionClient = notionClient,
        tokenClient = tokenClient,
        connectionReader = connectionReader,
        connectionStore = connectionStore,
    )

    "페이지 검색 pageSize를 1에서 100 사이로 보정한다" {
        // given
        val connection = notionPageServiceConnection(accessToken = "access-token")
        val pages = NotionPages(
            pages = listOf(NotionPageSummary(id = "page-id", title = "Title", url = null, lastEditedTime = null)),
            nextCursor = null,
            hasMore = false,
        )
        every {
            connectionReader.getByPublicIdAndWorkspaceId(
                publicId = "connection-id",
                workspaceId = 10L,
            )
        } returns connection
        every {
            notionClient.searchPages(
                accessToken = "access-token",
                query = "resume",
                startCursor = null,
                pageSize = 100,
            )
        } returns pages

        // when
        val result = notionPageService.searchPages(
            workspaceId = 10L,
            connectionPublicId = "connection-id",
            query = "resume",
            startCursor = null,
            pageSize = 300,
        )

        // then
        result shouldBe pages
        result.pages.map { it.id } shouldContainExactly listOf("page-id")
    }

    "Notion 인증 실패 시 토큰을 갱신하고 갱신된 토큰으로 한 번 재시도한다" {
        // given
        val connection = notionPageServiceConnection(accessToken = "expired-access-token", refreshToken = "refresh-token")
        val refreshedToken = NotionOAuthToken(
            accessToken = "refreshed-access-token",
            refreshToken = "refreshed-refresh-token",
            botId = "bot-id",
            notionWorkspaceId = "notion-workspace-id",
            workspaceName = "Jobdori",
            workspaceIcon = null,
        )
        val refreshedConnection = connection.refresh(
            accessToken = "refreshed-access-token",
            refreshToken = "refreshed-refresh-token",
        )
        val pages = NotionPages(
            pages = listOf(NotionPageSummary(id = "page-id", title = "Title", url = null, lastEditedTime = null)),
            nextCursor = "next-cursor",
            hasMore = true,
        )
        every {
            connectionReader.getByPublicIdAndWorkspaceId("connection-id", 10L)
        } returns connection
        every {
            notionClient.searchPages(
                accessToken = "expired-access-token",
                query = null,
                startCursor = null,
                pageSize = 20,
            )
        } throws NotionUnauthorizedException(message = "expired")
        every { tokenClient.refresh("refresh-token") } returns refreshedToken
        every { connectionStore.save(match { it.accessToken == "refreshed-access-token" }) } returns refreshedConnection
        every {
            notionClient.searchPages(
                accessToken = "refreshed-access-token",
                query = null,
                startCursor = null,
                pageSize = 20,
            )
        } returns pages

        // when
        val result = notionPageService.searchPages(
            workspaceId = 10L,
            connectionPublicId = "connection-id",
            query = null,
            startCursor = null,
            pageSize = 20,
        )

        // then
        result shouldBe pages
        verify(exactly = 1) { tokenClient.refresh("refresh-token") }
        verify(exactly = 1) { connectionStore.save(match { it.refreshToken == "refreshed-refresh-token" }) }
    }

    "토큰 갱신 후에도 인증에 실패하면 재연결 필요 예외를 던진다" {
        // given
        val connection = notionPageServiceConnection(accessToken = "expired-access-token", refreshToken = "refresh-token")
        val refreshedToken = NotionOAuthToken(
            accessToken = "refreshed-access-token",
            refreshToken = "refreshed-refresh-token",
            botId = "bot-id",
            notionWorkspaceId = "notion-workspace-id",
            workspaceName = "Jobdori",
            workspaceIcon = null,
        )
        every {
            connectionReader.getByPublicIdAndWorkspaceId("connection-id", 10L)
        } returns connection
        every {
            notionClient.searchPages(
                accessToken = "expired-access-token",
                query = null,
                startCursor = null,
                pageSize = 20,
            )
        } throws NotionUnauthorizedException(message = "expired")
        every { tokenClient.refresh("refresh-token") } returns refreshedToken
        every { connectionStore.save(match { it.accessToken == "refreshed-access-token" }) } returns connection.refresh(
            accessToken = "refreshed-access-token",
            refreshToken = "refreshed-refresh-token",
        )
        every {
            notionClient.searchPages(
                accessToken = "refreshed-access-token",
                query = null,
                startCursor = null,
                pageSize = 20,
            )
        } throws NotionUnauthorizedException(message = "still expired")

        // when & then
        shouldThrow<NotionConnectionNeedReconnectException> {
            notionPageService.searchPages(
                workspaceId = 10L,
                connectionPublicId = "connection-id",
                query = null,
                startCursor = null,
                pageSize = 20,
            )
        }
    }

})

private fun notionPageServiceConnection(
    id: Long = 1L,
    publicId: String = "connection-id",
    workspaceId: Long = 10L,
    accessToken: String = "access-token",
    refreshToken: String = "refresh-token",
) = NotionConnection(
    id = id,
    publicId = publicId,
    workspaceId = workspaceId,
    notionWorkspaceId = "notion-workspace-id",
    workspaceName = "Jobdori",
    workspaceIcon = null,
    botId = "bot-id",
    accessToken = accessToken,
    refreshToken = refreshToken,
    lastRefreshedAt = null,
)
