package com.jobdori.core.domain.profile.section

import com.jobdori.common.model.Period

data class Education(
    val school: String?,
    val major: String?,
    val degree: Degree?,
    val status: EducationStatus?,
    val period: Period?,
)

enum class Degree {
    BACHELOR,
    MASTER,
    DOCTOR,
}

enum class EducationStatus {
    ENROLLED,            // 재학
    ON_LEAVE,            // 휴학
    GRADUATED,           // 졸업
    EXPECTED_GRADUATION, // 졸업예정
    COMPLETED,           // 수료
}
