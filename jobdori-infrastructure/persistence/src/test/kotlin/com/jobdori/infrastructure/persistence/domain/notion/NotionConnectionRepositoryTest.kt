package com.jobdori.infrastructure.persistence.domain.notion

import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.repository.NotionConnectionRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.notion.repository.NotionConnectionJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@IntegrationTest
class NotionConnectionRepositoryTest(
    private val notionConnectionRepository: NotionConnectionRepository,
    private val notionConnectionJpaRepository: NotionConnectionJpaRepository,
) : StringSpec({

    afterEach {
        notionConnectionJpaRepository.deleteAll()
    }

    "Notion 연결을 암호화해서 저장하고 복호화된 도메인으로 조회한다" {
        // when
        val saved = notionConnectionRepository.save(
            notionConnection(
                id = 0L,
                publicId = "connection-id",
                workspaceId = 10L,
                accessToken = "plain-access-token",
                refreshToken = "plain-refresh-token",
            ),
        )

        // then
        val entities = notionConnectionJpaRepository.findAll()
        entities shouldHaveSize 1
        entities[0].accessTokenEncrypted shouldNotBe "plain-access-token"
        entities[0].refreshTokenEncrypted shouldNotBe "plain-refresh-token"

        val found = notionConnectionRepository.findByPublicIdAndWorkspaceId("connection-id", 10L)
        found?.id shouldBe saved.id
        found?.accessToken shouldBe "plain-access-token"
        found?.refreshToken shouldBe "plain-refresh-token"
    }

    "같은 워크스페이스와 Notion 봇 연결 저장은 기존 연결을 갱신한다" {
        // given
        val saved = notionConnectionRepository.save(
            notionConnection(
                id = 0L,
                publicId = "connection-id",
                workspaceId = 10L,
                workspaceName = "Old",
                accessToken = "old-access-token",
                refreshToken = "old-refresh-token",
            ),
        )

        // when
        val updated = notionConnectionRepository.save(
            notionConnection(
                id = 0L,
                publicId = "new-random-public-id",
                workspaceId = 10L,
                workspaceName = "Updated",
                accessToken = "updated-access-token",
                refreshToken = "updated-refresh-token",
            ),
        )

        // then
        updated.id shouldBe saved.id
        updated.publicId shouldBe "connection-id"
        updated.workspaceName shouldBe "Updated"
        updated.accessToken shouldBe "updated-access-token"
        updated.refreshToken shouldBe "updated-refresh-token"
        notionConnectionJpaRepository.findAll() shouldHaveSize 1
    }

    "워크스페이스의 Notion 연결 목록을 ID 역순 slice로 조회한다" {
        // given
        val first = notionConnectionRepository.save(
            notionConnection(id = 0L, publicId = "first", workspaceId = 10L, botId = "bot-1"),
        )
        val second = notionConnectionRepository.save(
            notionConnection(id = 0L, publicId = "second", workspaceId = 10L, botId = "bot-2"),
        )
        val third = notionConnectionRepository.save(
            notionConnection(id = 0L, publicId = "third", workspaceId = 10L, botId = "bot-3"),
        )
        notionConnectionRepository.save(
            notionConnection(id = 0L, publicId = "other-workspace", workspaceId = 20L, botId = "bot-4"),
        )

        // when
        val firstPage = notionConnectionRepository.findAllByWorkspaceId(
            workspaceId = 10L,
            cursorId = null,
            size = 2,
        )
        val nextPage = notionConnectionRepository.findAllByWorkspaceId(
            workspaceId = 10L,
            cursorId = second.id,
            size = 2,
        )

        // then
        firstPage.map { it.id } shouldContainExactly listOf(third.id, second.id)
        nextPage.map { it.id } shouldContainExactly listOf(first.id)
    }

    "공개 ID와 워크스페이스 ID로 삭제한다" {
        // given
        notionConnectionRepository.save(
            notionConnection(id = 0L, publicId = "connection-id", workspaceId = 10L),
        )

        // when
        notionConnectionRepository.deleteByPublicIdAndWorkspaceId(
            publicId = "connection-id",
            workspaceId = 10L,
        )

        // then
        notionConnectionRepository.findByPublicIdAndWorkspaceId("connection-id", 10L).shouldBeNull()
    }

})

private fun notionConnection(
    id: Long,
    publicId: String,
    workspaceId: Long,
    notionWorkspaceId: String = "notion-workspace-id",
    workspaceName: String? = "Jobdori",
    workspaceIcon: String? = "https://example.com/icon.png",
    botId: String = "bot-id",
    accessToken: String = "access-token",
    refreshToken: String = "refresh-token",
) = NotionConnection(
    id = id,
    publicId = publicId,
    workspaceId = workspaceId,
    notionWorkspaceId = notionWorkspaceId,
    workspaceName = workspaceName,
    workspaceIcon = workspaceIcon,
    botId = botId,
    accessToken = accessToken,
    refreshToken = refreshToken,
    lastRefreshedAt = null,
)
