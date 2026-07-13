package com.jobdori.core.domain.jdinsight.repository

import com.jobdori.core.domain.jdinsight.JdInsight

interface JdInsightRepository {
    fun save(insight: JdInsight): JdInsight
    fun findByJdId(jdId: Long): JdInsight?
    fun deleteByJdId(jdId: Long)
}
