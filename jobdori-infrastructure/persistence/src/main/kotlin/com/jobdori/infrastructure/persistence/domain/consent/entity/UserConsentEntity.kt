package com.jobdori.infrastructure.persistence.domain.consent.entity

import com.jobdori.core.domain.consent.ConsentSource
import com.jobdori.core.domain.consent.ConsentType
import com.jobdori.core.domain.consent.UserConsent
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Table(
    name = "user_consent_v1",
    indexes = [Index(name = "idx_user_consent_user_type_id", columnList = "user_id, type, id")],
)
@Entity
class UserConsentEntity(
    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: ConsentType,

    @Column(nullable = false)
    var agreed: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var source: ConsentSource,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = UserConsent(
        id = id,
        userId = userId,
        type = type,
        agreed = agreed,
        source = source,
        createdAt = createdAt,
    )

    companion object {
        fun from(domain: UserConsent) = UserConsentEntity(
            userId = domain.userId,
            type = domain.type,
            agreed = domain.agreed,
            source = domain.source,
        )
    }
}
