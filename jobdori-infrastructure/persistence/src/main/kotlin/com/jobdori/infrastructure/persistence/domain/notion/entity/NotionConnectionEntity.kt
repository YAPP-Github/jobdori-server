package com.jobdori.infrastructure.persistence.domain.notion.entity

import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.support.crypto.StringEncryptor
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
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
            name = "uk_notion_connection_public_id",
            columnNames = ["public_id"],
        ),
        UniqueConstraint(
            name = "uk_notion_connection_workspace_notion_workspace_bot",
            columnNames = ["workspace_id", "notion_workspace_id", "bot_id"],
        ),
    ],
)
@Entity
class NotionConnectionEntity(
    @Column(name = "public_id", nullable = false, updatable = false, length = 36)
    var publicId: String,

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
    var accessTokenEncrypted: String,

    @Column(name = "refresh_token_encrypted", nullable = false, columnDefinition = "text")
    var refreshTokenEncrypted: String,

    @Column(name = "last_refreshed_at")
    var lastRefreshedAt: LocalDateTime?,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun update(connection: NotionConnection, encryptor: StringEncryptor) {
        workspaceName = connection.workspaceName
        workspaceIcon = connection.workspaceIcon
        accessTokenEncrypted = encryptor.encrypt(connection.accessToken)
        refreshTokenEncrypted = encryptor.encrypt(connection.refreshToken)
        lastRefreshedAt = connection.lastRefreshedAt
    }

    fun toDomain(encryptor: StringEncryptor) = NotionConnection(
        id = id,
        publicId = publicId,
        workspaceId = workspaceId,
        notionWorkspaceId = notionWorkspaceId,
        workspaceName = workspaceName,
        workspaceIcon = workspaceIcon,
        botId = botId,
        accessToken = encryptor.decrypt(accessTokenEncrypted),
        refreshToken = encryptor.decrypt(refreshTokenEncrypted),
        lastRefreshedAt = lastRefreshedAt,
    )

    companion object {
        fun from(connection: NotionConnection, encryptor: StringEncryptor) = NotionConnectionEntity(
            publicId = connection.publicId,
            workspaceId = connection.workspaceId,
            notionWorkspaceId = connection.notionWorkspaceId,
            workspaceName = connection.workspaceName,
            workspaceIcon = connection.workspaceIcon,
            botId = connection.botId,
            accessTokenEncrypted = encryptor.encrypt(connection.accessToken),
            refreshTokenEncrypted = encryptor.encrypt(connection.refreshToken),
            lastRefreshedAt = connection.lastRefreshedAt,
        ).also {
            it.id = connection.id
        }
    }

}
