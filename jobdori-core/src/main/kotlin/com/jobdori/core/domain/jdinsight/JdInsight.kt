package com.jobdori.core.domain.jdinsight

import java.time.LocalDateTime

data class JdInsight(
    val id: Long,
    val jdId: Long,
    val keyPoints: String,
    val strategy: String,
    val createdAt: LocalDateTime? = null,
) {

    companion object {
        fun newInstance(
            jdId: Long,
            keyPoints: String,
            strategy: String,
        ) = JdInsight(
            id = 0L,
            jdId = jdId,
            keyPoints = keyPoints,
            strategy = strategy,
        )
    }

}
