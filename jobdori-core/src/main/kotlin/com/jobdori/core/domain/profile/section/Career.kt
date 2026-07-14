package com.jobdori.core.domain.profile.section

import com.jobdori.common.model.Period

data class Career(
    val company: String?,
    val position: String?,
    val period: Period?,
    val description: String?,
)
