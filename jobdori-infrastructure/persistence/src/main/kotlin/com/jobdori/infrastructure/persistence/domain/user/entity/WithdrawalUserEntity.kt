package com.jobdori.infrastructure.persistence.domain.user.entity

import com.jobdori.core.domain.user.WithdrawalUser
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "withdrawal_user_v1")
@Entity
class WithdrawalUserEntity(
    @Column(nullable = false)
    var originalUserId: Long,

    @Column(nullable = false, length = 50)
    var publicId: String,

) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    companion object {
        fun from(domain: WithdrawalUser) = WithdrawalUserEntity(
            originalUserId = domain.originalUserId,
            publicId = domain.publicId,
        )
    }

}
