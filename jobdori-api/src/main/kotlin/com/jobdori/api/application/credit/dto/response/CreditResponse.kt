package com.jobdori.api.application.credit.dto.response

import com.jobdori.core.domain.credit.CreditBalance
import com.jobdori.core.domain.credit.CreditPolicy

data class CreditResponse(
    val remaining: Int,
    val total: Int,
) {

    companion object {
        fun from(balance: CreditBalance) = CreditResponse(
            remaining = balance.remaining,
            total = CreditPolicy.DAILY_FREE,
        )
    }

}
