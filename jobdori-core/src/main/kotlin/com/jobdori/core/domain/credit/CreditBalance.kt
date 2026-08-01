package com.jobdori.core.domain.credit

import com.jobdori.core.domain.credit.error.InsufficientCreditException
import java.time.LocalDate

data class CreditBalance(
    val id: Long,
    val userId: Long,
    val remaining: Int,
    val lastResetDate: LocalDate,
) {

    fun resetIfNewDay(today: LocalDate): CreditBalance {
        return if (today.isAfter(lastResetDate)) {
            copy(
                remaining = CreditPolicy.DAILY_FREE,
                lastResetDate = today,
            )
        } else {
            this
        }
    }

    fun consume(cost: Int): CreditBalance {
        if (remaining <= 0) {
            throw InsufficientCreditException()
        }
        return copy(remaining = maxOf(0, remaining - cost))
    }

    companion object {
        fun newInstance(
            userId: Long,
            today: LocalDate,
        ) = CreditBalance(
            id = 0L,
            userId = userId,
            remaining = CreditPolicy.DAILY_FREE,
            lastResetDate = today,
        )
    }

}
