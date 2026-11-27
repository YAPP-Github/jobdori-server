package com.jobdori.core.application.consent

import com.jobdori.core.domain.consent.ConsentSource
import com.jobdori.core.domain.consent.ConsentType
import com.jobdori.core.domain.consent.UserConsent
import com.jobdori.core.domain.consent.error.ConsentAlreadyAgreedException
import com.jobdori.core.domain.consent.repository.ConsentSheetExposureRepository
import com.jobdori.core.domain.consent.repository.UserConsentRepository
import com.jobdori.core.domain.user.service.UserReader
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserConsentService(
    private val userConsentRepository: UserConsentRepository,
    private val userReader: UserReader,
    private val consentSheetExposureRepository: ConsentSheetExposureRepository,
) {

    @Transactional(readOnly = true)
    fun getConsents(userId: Long): List<UserConsent> = latestConsents(userConsentRepository.findAllByUserId(userId))

    @Transactional(readOnly = true)
    fun isConsentSheetRequired(userId: Long): Boolean {
        val consents = userConsentRepository.findAllByUserId(userId)
        return consents.any { it.source == ConsentSource.BACKFILL } &&
            !consentSheetExposureRepository.existsByUserId(userId) &&
            consents.none {
                it.type in setOf(ConsentType.RESEARCH, ConsentType.MARKETING_EMAIL) &&
                    it.source != ConsentSource.BACKFILL
            }
    }

    @Transactional
    fun agreeSignupConsents(
        userId: Long,
        research: Boolean?,
        marketingEmail: Boolean?,
    ): List<UserConsent> {
        val existing = userConsentRepository.findAllByUserId(userId)
        if (existing.any { it.type == ConsentType.TERMS }) throw ConsentAlreadyAgreedException()
        val newConsents = listOf(
            newConsent(userId, ConsentType.TERMS, true, ConsentSource.SIGNUP),
            newConsent(userId, ConsentType.PRIVACY, true, ConsentSource.SIGNUP),
            newConsent(userId, ConsentType.RESEARCH, research ?: false, ConsentSource.SIGNUP),
            newConsent(userId, ConsentType.MARKETING_EMAIL, marketingEmail ?: false, ConsentSource.SIGNUP),
        )
        userConsentRepository.saveAll(newConsents)
        return getConsents(userId)
    }

    @Transactional
    fun updateOptionalConsents(
        userId: Long,
        research: Boolean?,
        marketingEmail: Boolean?,
        source: ConsentSource,
    ): List<UserConsent> {
        val updates = buildList {
            research?.let { add(newConsent(userId, ConsentType.RESEARCH, it, source)) }
            marketingEmail?.let { add(newConsent(userId, ConsentType.MARKETING_EMAIL, it, source)) }
        }
        userConsentRepository.saveAll(updates)
        return getConsents(userId)
    }

    fun markConsentSheetShown(userId: Long): Boolean {
        userReader.getUser(userId)
        if (!consentSheetExposureRepository.existsByUserId(userId)) {
            try {
                consentSheetExposureRepository.save(userId)
            } catch (e: DataIntegrityViolationException) {
                // 동시 요청으로 이미 노출 기록이 저장된 경우 무시한다.
            }
        }
        return true
    }

    private fun latestConsents(consents: List<UserConsent>): List<UserConsent> = consents
        .groupBy { it.type }
        .values
        .map { rows -> rows.maxBy { it.id } }
        .sortedBy { it.type.ordinal }

    private fun newConsent(userId: Long, type: ConsentType, agreed: Boolean, source: ConsentSource) =
        UserConsent(id = 0L, userId = userId, type = type, agreed = agreed, source = source)
}
