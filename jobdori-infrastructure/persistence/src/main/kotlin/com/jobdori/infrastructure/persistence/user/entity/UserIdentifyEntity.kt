package com.jobdori.infrastructure.persistence.user.entity

import com.jobdori.core.domain.user.UserIdentify
import com.jobdori.core.domain.user.UserIdentifyProvider
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

@Entity
@Table(
    name = "user_identifies",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_identifies_provider_identify_id",
            columnNames = ["identify_id", "identify_provider"],
        ),
    ],
)
class UserIdentifyEntity(
    @Column(name = "user_id", nullable = false)
    var userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "identify_provider", nullable = false, length = 30)
    var identifyProvider: UserIdentifyProvider,

    @Column(name = "identify_id", nullable = false)
    var identifyId: String,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = UserIdentify(
        id = this.id,
        userId = this.userId,
        identifyProvider = this.identifyProvider,
        identifyId = this.identifyId,
    )

    companion object {
        fun from(
            domain: UserIdentify,
        ) = UserIdentifyEntity(
            userId = domain.userId,
            identifyProvider = domain.identifyProvider,
            identifyId = domain.identifyId,
        ).apply { id = domain.id }
    }

}
