package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.token.AuthTokenProvider
import com.jobdori.core.application.auth.token.AuthTokenType
import com.jobdori.core.application.auth.error.InvalidAuthTokenException
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.service.UserReader
import org.springframework.stereotype.Service

@Service
class AuthUserReadService(
    private val authTokenProvider: AuthTokenProvider,
    private val userReader: UserReader,
) {

    fun getUserId(accessToken: String): Long {
        val userPublicId = authTokenProvider.parse(
            token = accessToken,
            expectedType = AuthTokenType.ACCESS,
        ).userId

        val user = try {
            userReader.getUser(publicId = userPublicId)
        } catch (exception: UserNotFoundException) {
            throw InvalidAuthTokenException(
                message = "인증 토큰의 사용자 정보를 찾을 수 없습니다 [userPublicId=$userPublicId]",
                cause = exception,
            )
        }

        return user.id
    }

}
