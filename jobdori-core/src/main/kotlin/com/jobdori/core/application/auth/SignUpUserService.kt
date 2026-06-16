package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.error.AlreadySignedUpException
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentify
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.repository.UserIdentifyRepository
import com.jobdori.core.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignUpUserService(
    private val userRepository: UserRepository,
    private val userIdentifyRepository: UserIdentifyRepository,
) {

    @Transactional
    fun signUp(
        provider: UserIdentifyProvider,
        identifyId: String,
    ): User {
        val existingIdentify = userIdentifyRepository.findByProviderAndIdentifyId(
            provider = provider,
            identifyId = identifyId,
        )
        if (existingIdentify != null) {
            throw AlreadySignedUpException("이미 가입된 게정입니다 [provider=$provider,identifyId=$identifyId]")
        }

        val user = userRepository.save(User.newInstance())
        userIdentifyRepository.save(
            UserIdentify.newInstance(
                user = user,
                identifyProvider = provider,
                identifyId = identifyId,
            ),
        )
        return user
    }

}
