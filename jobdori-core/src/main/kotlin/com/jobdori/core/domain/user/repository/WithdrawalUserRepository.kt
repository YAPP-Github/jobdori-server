package com.jobdori.core.domain.user.repository

import com.jobdori.core.domain.user.WithdrawalUser

interface WithdrawalUserRepository {

    fun save(withdrawalUser: WithdrawalUser): WithdrawalUser

}
