package com.jobdori.infrastructure.persistence.domain.user.entity

import com.jobdori.core.domain.user.WithdrawalUser
import com.jobdori.core.domain.user.WithdrawalUserIdentity
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Table(name = "withdrawal_user_v1")
@Entity
class WithdrawalUserEntity(
    @Column(nullable = false)
    var originalUserId: Long,

    @Column(nullable = false, length = 50)
    var publicId: String,

    @Column(nullable = false, length = 320)
    var email: String,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(length = 300)
    var profileImageUrl: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var userIdentities: List<WithdrawalUserIdentity>,

    @Column(nullable = false)
    var userCreatedAt: LocalDateTime,

    @Column(nullable = false)
    var userUpdatedAt: LocalDateTime,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    companion object {
        fun from(domain: WithdrawalUser) = WithdrawalUserEntity(
            originalUserId = domain.originalUserId,
            publicId = domain.publicId,
            email = domain.email,
            name = domain.name,
            profileImageUrl = domain.profileImageUrl,
            userIdentities = domain.userIdentities,
            userCreatedAt = domain.userCreatedAt,
            userUpdatedAt = domain.userUpdatedAt,
        )
    }

}
