package com.jobdori.infrastructure.persistence.domain.jdinsight.repository

import com.jobdori.core.domain.jdinsight.JdInsight
import com.jobdori.core.domain.jdinsight.repository.JdInsightRepository
import com.jobdori.infrastructure.persistence.domain.jdinsight.entity.JdInsightEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JdInsightRepositoryImpl(
    private val jdInsightJpa: JdInsightJpaRepository,
) : JdInsightRepository {

    @Transactional
    override fun save(insight: JdInsight): JdInsight =
        jdInsightJpa.save(JdInsightEntity.from(insight)).toDomain()

    @Transactional(readOnly = true)
    override fun findByJdId(jdId: Long): JdInsight? =
        jdInsightJpa.findByJdId(jdId)?.toDomain()

}
