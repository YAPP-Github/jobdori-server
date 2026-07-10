package com.jobdori.infrastructure.persistence.domain.jdinsight.repository

import com.jobdori.infrastructure.persistence.domain.jdinsight.entity.JdInsightEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JdInsightJpaRepository : JpaRepository<JdInsightEntity, Long> {
    fun findByJdId(jdId: Long): JdInsightEntity?
}
