package com.jobdori.core.domain.credit

import com.jobdori.core.domain.credit.error.InsufficientCreditException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class CreditBalanceTest : StringSpec({

    val today = LocalDate.of(2026, 7, 30)

    "마지막 리셋 날짜가 어제면 오늘의 무료 크레딧으로 리셋한다" {
        val balance = creditBalance(lastResetDate = today.minusDays(1))

        balance.resetIfNewDay(today) shouldBe balance.copy(
            remaining = CreditPolicy.DAILY_FREE,
            lastResetDate = today,
        )
    }

    "마지막 리셋 날짜가 오늘이면 잔액을 유지한다" {
        val balance = creditBalance(lastResetDate = today)

        balance.resetIfNewDay(today) shouldBe balance
    }

    "잔여 크레딧보다 큰 비용을 차감하면 0으로 보정한다" {
        val balance = creditBalance(remaining = 1, lastResetDate = today)

        balance.consume(10).remaining shouldBe 0
    }

    "잔여 크레딧이 0이면 차감을 거부한다" {
        val balance = creditBalance(remaining = 0, lastResetDate = today)

        shouldThrow<InsufficientCreditException> {
            balance.consume(1)
        }
    }

})

private fun creditBalance(
    remaining: Int = 3,
    lastResetDate: LocalDate,
) = CreditBalance(
    id = 1L,
    userId = 10L,
    remaining = remaining,
    lastResetDate = lastResetDate,
)
