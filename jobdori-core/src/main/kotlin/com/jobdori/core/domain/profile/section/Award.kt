package com.jobdori.core.domain.profile.section

import java.time.LocalDate

data class Award(
    val title: String?,
    val organization: String?,
    val awardedAt: LocalDate?,
)
