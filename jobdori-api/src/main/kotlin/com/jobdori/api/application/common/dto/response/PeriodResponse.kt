package com.jobdori.api.application.common.dto.response

import com.jobdori.common.model.Period
import java.time.LocalDate

data class PeriodResponse(
    val startAt: LocalDate?,
    val endAt: LocalDate?,
) {

    companion object {
        fun from(period: Period) = PeriodResponse(
            startAt = period.startAt,
            endAt = period.endAt,
        )
    }

}
