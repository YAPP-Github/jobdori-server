package com.jobdori.api.application.consent.dto.response

import com.jobdori.common.time.toInstantAtSystemDefaultZone
import com.jobdori.core.domain.consent.ConsentType
import com.jobdori.core.domain.consent.UserConsent
import java.time.Instant
import java.time.temporal.ChronoUnit

data class UserConsentResponse(
    val type: ConsentType,
    val agreed: Boolean,
    val changedAt: Instant,
) {
    companion object {
        fun from(consent: UserConsent) = UserConsentResponse(
            type = consent.type,
            agreed = consent.agreed,
            // DB timestamp(6)에 맞춰 저장 직후 응답과 이후 조회값이 같게 한다
            changedAt = requireNotNull(consent.createdAt) { "저장되지 않은 동의입니다." }
                .truncatedTo(ChronoUnit.MICROS)
                .toInstantAtSystemDefaultZone(),
        )
    }
}
