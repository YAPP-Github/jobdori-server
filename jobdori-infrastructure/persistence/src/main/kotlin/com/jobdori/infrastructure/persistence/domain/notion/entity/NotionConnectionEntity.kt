package com.jobdori.infrastructure.persistence.domain.notion.entity

import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.converter.EncryptedStringConverter
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Table(
    name = "notion_connection_v1",
    indexes = [
        Index(name = "idx_notion_connection_workspace", columnList = "workspace_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_notion_connection_workspace_notion_workspace_bot",
            columnNames = ["workspace_id", "notion_workspace_id", "bot_id"],
        ),
    ],
)
@Entity
class NotionConnectionEntity(
    @Column(name = "workspace_id", nullable = false, updatable = false)
    var workspaceId: Long,

    @Column(name = "notion_workspace_id", nullable = false, length = 100)
    var notionWorkspaceId: String,

    @Column(name = "workspace_name", length = 200)
    var workspaceName: String?,

    @Column(name = "workspace_icon", length = 1000)
    var workspaceIcon: String?,

    @Column(name = "bot_id", nullable = false, length = 100)
    var botId: String,

    @Column(name = "access_token_encrypted", nullable = false, columnDefinition = "text")
    @Convert(converter = EncryptedStringConverter::class)
    var accessToken: String,

    @Column(name = "refresh_token_encrypted", nullable = false, columnDefinition = "text")
    @Convert(converter = EncryptedStringConverter::class)
    var refreshToken: String,

    @Column(name = "last_refreshed_at")
    var lastRefreshedAt: LocalDateTime?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun update(connection: NotionConnection) {
        workspaceName = connection.workspaceName
        workspaceIcon = connection.workspaceIcon
        accessToken = connection.accessToken
        refreshToken = connection.refreshToken
        lastRefreshedAt = connection.lastRefreshedAt
    }

    fun toDomain() = NotionConnection(
        id = id,
        workspaceId = workspaceId,
        notionWorkspaceId = notionWorkspaceId,
        workspaceName = workspaceName,
        workspaceIcon = workspaceIcon,
        botId = botId,
        accessToken = accessToken,
        refreshToken = refreshToken,
        lastRefreshedAt = lastRefreshedAt,
    )

    companion object {
        fun from(connection: NotionConnection) = NotionConnectionEntity(
            workspaceId = connection.workspaceId,
            notionWorkspaceId = connection.notionWorkspaceId,
            workspaceName = connection.workspaceName,
            workspaceIcon = connection.workspaceIcon,
            botId = connection.botId,
            accessToken = connection.accessToken,
            refreshToken = connection.refreshToken,
            lastRefreshedAt = connection.lastRefreshedAt,
        ).also {
            it.id = connection.id
        }
    }

}
