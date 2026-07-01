package com.jobdori.infrastructure.persistence.domain.user.entity

import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Table(
    name = "user_identity_v1",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_identity_provider_provider_user_id",
            columnNames = ["provider", "provider_user_id"],
        ),
    ],
)
@Entity
class UserIdentityEntity(
    @Column(nullable = false)
    var userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var provider: UserIdentityProvider,

    @Column(nullable = false, length = 100)
    var providerUserId: String,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = UserIdentity(
        id = this.id,
        userId = this.userId,
        provider = this.provider,
        providerUserId = this.providerUserId,
    )

    companion object {
        fun from(
            domain: UserIdentity,
        ) = UserIdentityEntity(
            userId = domain.userId,
            provider = domain.provider,
            providerUserId = domain.providerUserId,
        ).apply { id = domain.id }
    }

}
