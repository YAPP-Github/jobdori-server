package com.jobdori.core.domain.credit.repository

import com.jobdori.core.domain.credit.CreditBalance

interface CreditRepository {

    fun findByUserId(userId: Long): CreditBalance?

    fun findByUserIdForUpdate(userId: Long): CreditBalance?

    fun save(balance: CreditBalance): CreditBalance

}
