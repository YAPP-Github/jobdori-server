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
    @AssertTrue(message = "startAt must be before or equal to endAt")
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
