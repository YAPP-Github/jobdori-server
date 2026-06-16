package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.UserIdentify
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.repository.UserIdentifyRepository
import org.springframework.stereotype.Service

@Service
class UserIdentifyReader(
    private val userIdentifyRepository: UserIdentifyRepository,
) {

    fun findIdentify(provider: UserIdentifyProvider, identifyId: String): UserIdentify? {
        return userIdentifyRepository.findByProviderAndIdentifyId(provider, identifyId)
    }

}
