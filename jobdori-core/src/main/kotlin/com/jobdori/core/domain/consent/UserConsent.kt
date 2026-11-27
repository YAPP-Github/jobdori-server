package com.jobdori.core.domain.consent

import java.time.LocalDateTime

enum class ConsentType { TERMS, PRIVACY, RESEARCH, MARKETING_EMAIL }

enum class ConsentSource { SIGNUP, MYPAGE, EXISTING_USER_SHEET, BACKFILL }

data class UserConsent(
    val id: Long,
    val userId: Long,
    val type: ConsentType,
    val agreed: Boolean,
    val source: ConsentSource,
    val createdAt: LocalDateTime? = null,
)
