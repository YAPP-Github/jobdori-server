package com.jobdori.core.domain.consent.repository

import com.jobdori.core.domain.consent.UserConsent

interface UserConsentRepository {
    fun findAllByUserId(userId: Long): List<UserConsent>

    fun saveAll(consents: List<UserConsent>): List<UserConsent>
}
