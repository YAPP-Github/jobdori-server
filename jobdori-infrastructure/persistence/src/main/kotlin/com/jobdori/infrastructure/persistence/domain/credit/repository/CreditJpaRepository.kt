package com.jobdori.infrastructure.persistence.domain.credit.repository

import com.jobdori.infrastructure.persistence.domain.credit.entity.CreditEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface CreditJpaRepository : JpaRepository<CreditEntity, Long> {

    fun findByUserId(userId: Long): CreditEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findFirstByUserId(userId: Long): CreditEntity?

}
