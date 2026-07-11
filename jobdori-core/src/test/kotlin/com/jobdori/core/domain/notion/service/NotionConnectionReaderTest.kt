package com.jobdori.core.domain.notion.service

import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.error.NotionConnectionNotFoundException
import com.jobdori.core.domain.notion.repository.NotionConnectionRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class NotionConnectionReaderTest : StringSpec({

    val connectionRepository = mockk<NotionConnectionRepository>()
    val connectionReader = NotionConnectionReader(connectionRepository)

    "공개 ID와 워크스페이스 ID로 Notion 연결을 조회한다" {
        // given
        val connection = notionReaderConnection(id = 1L, publicId = "connection-id", workspaceId = 10L)
        every {
            connectionRepository.findByPublicIdAndWorkspaceId(
                publicId = "connection-id",
                workspaceId = 10L,
            )
        } returns connection

        // when & then
        connectionReader.getByPublicIdAndWorkspaceId(
            publicId = "connection-id",
            workspaceId = 10L,
        ) shouldBe connection
    }

    "Notion 연결이 없으면 예외를 던진다" {
        // given
        every {
            connectionRepository.findByPublicIdAndWorkspaceId(
                publicId = "missing-connection-id",
                workspaceId = 10L,
            )
        } returns null

        // when & then
        shouldThrow<NotionConnectionNotFoundException> {
            connectionReader.getByPublicIdAndWorkspaceId(
                publicId = "missing-connection-id",
                workspaceId = 10L,
            )
        }
    }

    "워크스페이스의 Notion 연결 목록을 slice로 조회한다" {
        // given
        val connections = listOf(
            notionReaderConnection(id = 3L, workspaceId = 10L),
            notionReaderConnection(id = 2L, workspaceId = 10L),
            notionReaderConnection(id = 1L, workspaceId = 10L),
        )
        every {
            connectionRepository.findAllByWorkspaceId(
                workspaceId = 10L,
                cursorId = 4L,
                size = 3,
            )
        } returns connections

        // when
        val result = connectionReader.findAllByWorkspaceId(
            workspaceId = 10L,
            cursor = "4",
            size = 2,
        )

        // then
        result.items shouldContainExactly connections.take(2)
        result.nextCursor shouldBe "2"
    }

    "숫자가 아닌 cursor는 첫 페이지 조회로 처리한다" {
        // given
        every {
            connectionRepository.findAllByWorkspaceId(
                workspaceId = 10L,
                cursorId = null,
                size = 3,
            )
        } returns listOf(
            notionReaderConnection(id = 3L, workspaceId = 10L),
            notionReaderConnection(id = 2L, workspaceId = 10L),
        )

        // when
        val result = connectionReader.findAllByWorkspaceId(
            workspaceId = 10L,
            cursor = "invalid-cursor",
            size = 2,
        )

        // then
        result.items.map { it.id } shouldContainExactly listOf(3L, 2L)
        result.nextCursor shouldBe null
    }

})

private fun notionReaderConnection(
    id: Long = 1L,
    publicId: String = "connection-$id",
    workspaceId: Long = 1L,
) = NotionConnection(
    id = id,
    publicId = publicId,
    workspaceId = workspaceId,
    notionWorkspaceId = "notion-workspace-id",
    workspaceName = "Jobdori",
    workspaceIcon = "https://example.com/icon.png",
    botId = "bot-id",
    accessToken = "access-token",
    refreshToken = "refresh-token",
    lastRefreshedAt = null,
)
