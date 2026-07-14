package com.jobdori.core.domain.profile.section

import java.time.LocalDate

data class Certification(
    val name: String?,
    val issuer: String?,
    val acquiredAt: LocalDate?,
)
