package com.jobdori.infrastructure.persistence.domain.user.repository

import com.jobdori.core.domain.user.WithdrawalUser
import com.jobdori.core.domain.user.repository.WithdrawalUserRepository
import com.jobdori.infrastructure.persistence.domain.user.entity.WithdrawalUserEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class WithdrawalUserRepositoryImpl(
    private val jpaRepository: WithdrawalUserJpaRepository,
) : WithdrawalUserRepository {

    @Transactional
    override fun save(withdrawalUser: WithdrawalUser): WithdrawalUser {
        val saved = jpaRepository.save(WithdrawalUserEntity.from(withdrawalUser))
        return withdrawalUser.copy(id = saved.id)
    }

}
