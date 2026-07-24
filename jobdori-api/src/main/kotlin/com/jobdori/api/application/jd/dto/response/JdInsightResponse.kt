package com.jobdori.api.application.jd.dto.response

import com.jobdori.core.domain.jd.Jd

data class JdInsightResponse(
    val strategy: String,
) {

    companion object {
        fun from(jd: Jd) = JdInsightResponse(
            strategy = jd.strategy,
        )
    }

}
