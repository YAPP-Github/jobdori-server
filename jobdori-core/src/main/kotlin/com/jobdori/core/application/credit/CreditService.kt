package com.jobdori.core.application.credit

import com.jobdori.core.domain.credit.CreditBalance
import com.jobdori.core.domain.credit.CreditFeature
import com.jobdori.core.domain.credit.CreditPolicy
import com.jobdori.core.domain.credit.repository.CreditRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

@Service
class CreditService(
    private val creditRepository: CreditRepository,
    transactionManager: PlatformTransactionManager,
) {

    private val consumeTransaction = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    @Transactional(readOnly = true)
    fun getBalance(userId: Long): CreditBalance {
        val today = LocalDate.now(CreditPolicy.ZONE)
        // 조회에서 저장하면 동시 차감 결과를 오래된 잔액으로 덮어쓸 수 있다.
        return (creditRepository.findByUserId(userId) ?: CreditBalance.newInstance(userId, today))
            .resetIfNewDay(today)
    }

    // 각 시도는 별도 트랜잭션으로 커밋해 AI 결과와 무관한 차감과 unique 경합 재시도를 보장한다.
    fun consume(userId: Long, feature: CreditFeature) {
        val today = LocalDate.now(CreditPolicy.ZONE)
        try {
            consumeOnce(userId, feature, today)
        } catch (_: DataIntegrityViolationException) {
            consumeOnce(userId, feature, today)
        }
    }

    private fun consumeOnce(userId: Long, feature: CreditFeature, today: LocalDate) {
        consumeTransaction.executeWithoutResult {
            val balance = creditRepository.findByUserIdForUpdate(userId)
                ?: CreditBalance.newInstance(userId, today)
            creditRepository.save(
                balance
                    .resetIfNewDay(today)
                    .consume(feature.cost),
            )
        }
    }

}
