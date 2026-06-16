package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.error.UserAlreadyExistsException
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import com.jobdori.core.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserCreator(
    private val userRepository: UserRepository,
    private val userIdentityRepository: UserIdentityRepository,
) {

    @Transactional
    fun create(
        provider: UserIdentityProvider,
        providerUserId: String,
    ): User {
        val existingIdentity = userIdentityRepository.existsByProviderAndProviderUserId(
            provider = provider,
            providerUserId = providerUserId,
        )
        if (existingIdentity) {
            throw UserAlreadyExistsException("이미 가입된 게정입니다 [provider=$provider,providerUserId=$providerUserId]")
        }

        val user = userRepository.save(User.newInstance())
        userIdentityRepository.save(
            UserIdentity.newInstance(
                user = user,
                provider = provider,
                providerUserId = providerUserId,
            ),
        )
        return user
    }

}
