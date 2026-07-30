package com.jobdori.infrastructure.persistence.domain.credit.entity

import com.jobdori.core.domain.credit.CreditBalance
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Table(name = "credit_v1")
@Entity
class CreditEntity(
    @Column(nullable = false, unique = true, updatable = false)
    var userId: Long,

    @Column(nullable = false)
    var remaining: Int,

    @Column(nullable = false)
    var lastResetDate: LocalDate,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toDomain() = CreditBalance(
        id = id,
        userId = userId,
        remaining = remaining,
        lastResetDate = lastResetDate,
    )

    companion object {
        fun from(balance: CreditBalance) = CreditEntity(
            userId = balance.userId,
            remaining = balance.remaining,
            lastResetDate = balance.lastResetDate,
        ).also { it.id = balance.id }
    }

}
