package com.jobdori.infrastructure.persistence.domain.credit.repository

import com.jobdori.core.domain.credit.CreditBalance
import com.jobdori.core.domain.credit.repository.CreditRepository
import com.jobdori.infrastructure.persistence.domain.credit.entity.CreditEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class CreditRepositoryImpl(
    private val jpaRepository: CreditJpaRepository,
) : CreditRepository {

    @Transactional(readOnly = true)
    override fun findByUserId(userId: Long): CreditBalance? {
        return jpaRepository.findByUserId(userId)
            ?.toDomain()
    }

    // 호출부의 쓰기 트랜잭션에 참여해야 PESSIMISTIC_WRITE 락이 유지되므로 @Transactional을 붙이지 않는다.
    override fun findByUserIdForUpdate(userId: Long): CreditBalance? {
        return jpaRepository.findFirstByUserId(userId)
            ?.toDomain()
    }

    @Transactional
    override fun save(balance: CreditBalance): CreditBalance {
        val entity = jpaRepository.save(CreditEntity.from(balance))
        return entity.toDomain()
    }

}
