package com.jobdori.api.application.jd.dto.response

import com.jobdori.core.domain.jdinsight.JdInsight

data class JdInsightResponse(
    val keyPoints: String,
    val strategy: String,
) {

    companion object {
        fun from(insight: JdInsight) = JdInsightResponse(
            keyPoints = insight.keyPoints,
            strategy = insight.strategy,
        )
    }

}
