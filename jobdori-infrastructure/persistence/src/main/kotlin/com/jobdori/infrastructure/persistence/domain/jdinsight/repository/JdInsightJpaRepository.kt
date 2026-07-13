package com.jobdori.infrastructure.persistence.domain.jdinsight.repository

import com.jobdori.infrastructure.persistence.domain.jdinsight.entity.JdInsightEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JdInsightJpaRepository : JpaRepository<JdInsightEntity, Long> {
    fun findByJdId(jdId: Long): JdInsightEntity?

    @Modifying
    @Query("delete from JdInsightEntity e where e.jdId = :jdId")
    fun deleteByJdId(@Param("jdId") jdId: Long)
}
