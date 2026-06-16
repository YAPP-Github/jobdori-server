package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.oauth.google.GoogleAuthProcessor
import com.jobdori.core.application.auth.token.AuthTokenPair
import com.jobdori.core.application.auth.token.AuthTokenProvider
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.service.UserIdentifyReader
import com.jobdori.core.domain.user.service.UserReader
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val googleAuthProcessor: GoogleAuthProcessor,
    private val userIdentifyReader: UserIdentifyReader,
    private val userReader: UserReader,
    private val authTokenProvider: AuthTokenProvider,
) {

    fun login(command: AuthCommand): AuthTokenPair {
        val identifyId = getIdentifyId(command)
        val userIdentify = userIdentifyReader.findIdentify(
            provider = command.provider,
            identifyId = identifyId,
        ) ?: throw UserNotFoundException(
            "${command.provider} 계정으로 가입된 사용자를 찾을 수 없습니다",
        )

        val user = userReader.getUser(userIdentify.userId)
        return authTokenProvider.issue(user.publicId)
    }

    private fun getIdentifyId(command: AuthCommand): String =
        when (command.provider) {
            UserIdentifyProvider.GOOGLE -> googleAuthProcessor.getGoogleUserId(command).value
        }

}
