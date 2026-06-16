package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.oauth.google.GoogleAuthProcessor
import com.jobdori.core.domain.auth.AuthTokenPair
import com.jobdori.core.domain.auth.service.AuthTokenProvider
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.service.UserCreator
import com.jobdori.core.domain.user.service.UserIdentityReader
import com.jobdori.core.domain.user.service.UserReader
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val googleAuthProcessor: GoogleAuthProcessor,
    private val userCreator: UserCreator,
    private val userIdentityReader: UserIdentityReader,
    private val userReader: UserReader,
    private val authTokenProvider: AuthTokenProvider,
) {

    fun signUp(command: AuthCommand): AuthTokenPair {
        val providerUserId = getProviderUserId(command)
        val user = userCreator.create(
            provider = command.provider,
            providerUserId = providerUserId,
        )
        return authTokenProvider.issue(user.publicId)
    }

    fun login(command: AuthCommand): AuthTokenPair {
        val providerUserId = getProviderUserId(command)
        val userIdentity = userIdentityReader.findIdentity(
            provider = command.provider,
            providerUserId = providerUserId,
        ) ?: throw UserNotFoundException(
            "${command.provider} 계정으로 가입된 사용자를 찾을 수 없습니다",
        )

        val user = userReader.getUser(userIdentity.userId)
        return authTokenProvider.issue(user.publicId)
    }

    private fun getProviderUserId(command: AuthCommand): String {
        return when (command.provider) {
            UserIdentityProvider.GOOGLE -> googleAuthProcessor.getGoogleUserId(command).value
        }
    }

}
