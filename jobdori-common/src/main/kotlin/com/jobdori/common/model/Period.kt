package com.jobdori.common.model

import java.time.LocalDate

data class Period(
    val startAt: LocalDate?,
    val endAt: LocalDate?,
) {

    init {
        if (startAt != null && endAt != null) {
            require(startAt <= endAt) {
                "시작 일자($startAt)은 종료 일자($endAt)보다 같거나 이전이이여 합니다.)"
            }
        }
    }

}
