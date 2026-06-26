package com.jobdori.infrastructure.persistence.workspace.entity

import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Table(
    name = "workspace_v1",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_workspace_owner_user_id",
            columnNames = ["owner_user_id"],
        ),
    ],
)
@Entity
class WorkspaceEntity(
    @Column(nullable = false, length = 50, unique = true, updatable = false)
    var publicId: String,

    @Column(nullable = false, updatable = false)
    var ownerUserId: Long,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = Workspace(
        id = id,
        publicId = publicId,
        ownerUserId = ownerUserId,
    )

    companion object {
        fun from(workspace: Workspace) = WorkspaceEntity(
            publicId = workspace.publicId,
            ownerUserId = workspace.ownerUserId,
        ).also { it.id = workspace.id }
    }

}
