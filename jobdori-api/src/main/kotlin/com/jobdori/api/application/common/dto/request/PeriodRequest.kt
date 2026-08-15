package com.jobdori.api.application.common.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.common.model.Period
import jakarta.validation.constraints.AssertTrue
import java.time.LocalDate

data class PeriodRequest(
    val startAt: LocalDate?,
    val endAt: LocalDate?,
) {

    @JsonIgnore
    @AssertTrue(message = "시작일이 종료일보다 늦어요. 날짜를 다시 확인해 주세요.")
    fun isValidPeriod(): Boolean {
        if (startAt == null || endAt == null) {
            return true
        }

        return !startAt.isAfter(endAt)
    }

    fun toPeriod() = Period(
        startAt = startAt,
        endAt = endAt
    )

}
