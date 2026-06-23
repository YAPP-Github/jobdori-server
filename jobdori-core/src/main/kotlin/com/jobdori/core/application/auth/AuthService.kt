package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.oauth.google.GoogleAuthProcessor
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserInfo
import com.jobdori.core.application.auth.result.AuthResult
import com.jobdori.core.domain.auth.service.AuthTokenProvider
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
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

    fun login(command: AuthCommand): AuthResult {
        val googleUserInfo = getGoogleUserInfo(command)
        val providerUserId = googleUserInfo.id
        val userIdentity = userIdentityReader.findIdentity(
            provider = command.provider,
            providerUserId = providerUserId,
        )
        val user = getOrCreateUser(userIdentity = userIdentity, command = command, googleUserInfo = googleUserInfo)
        return AuthResult(
            tokenPair = authTokenProvider.issue(user.publicId),
            isNewUser = userIdentity == null,
        )
    }

    private fun getGoogleUserInfo(command: AuthCommand): GoogleUserInfo {
        return when (command.provider) {
            UserIdentityProvider.GOOGLE -> googleAuthProcessor.getGoogleUserInfo(command)
        }
    }

    private fun getOrCreateUser(
        userIdentity: UserIdentity?,
        command: AuthCommand,
        googleUserInfo: GoogleUserInfo,
    ): User {
        if (userIdentity == null) {
            return userCreator.create(
                provider = command.provider,
                providerUserId = googleUserInfo.id,
                name = googleUserInfo.name,
                profileImageUrl = googleUserInfo.profileImageUrl,
            )
        }
        return userReader.getUser(userIdentity.userId)
    }

}
