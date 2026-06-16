package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.oauth.google.GoogleAuthProcessor
import com.jobdori.core.application.auth.token.AuthTokenPair
import com.jobdori.core.application.auth.token.AuthTokenProvider
import com.jobdori.core.domain.user.UserIdentifyProvider
import org.springframework.stereotype.Service

@Service
class SignUpService(
    private val googleAuthProcessor: GoogleAuthProcessor,
    private val signUpUserService: SignUpUserService,
    private val authTokenProvider: AuthTokenProvider,
) {

    fun signUp(command: AuthCommand): AuthTokenPair {
        val identifyId = getIdentifyId(command)
        val user = signUpUserService.signUp(
            provider = command.provider,
            identifyId = identifyId,
        )
        return authTokenProvider.issue(user.publicId)
    }

    private fun getIdentifyId(command: AuthCommand): String =
        when (command.provider) {
            UserIdentifyProvider.GOOGLE -> googleAuthProcessor.getGoogleUserId(command).value
        }

}
