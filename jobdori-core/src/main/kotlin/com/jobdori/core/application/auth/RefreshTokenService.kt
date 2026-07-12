package com.jobdori.core.application.auth

import com.jobdori.core.domain.auth.AuthToken
import com.jobdori.core.domain.auth.AuthTokenType
import com.jobdori.core.domain.auth.error.InvalidAuthTokenException
import com.jobdori.core.domain.auth.service.AuthTokenProvider
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.service.UserReader
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
    private val authTokenProvider: AuthTokenProvider,
    private val userReader: UserReader,
) {

    fun refresh(refreshToken: String): AuthToken {
        val user = getRefreshTokenUser(refreshToken)
        return authTokenProvider.issueAccessToken(user.publicId)
    }

    fun validate(refreshToken: String) {
        getRefreshTokenUser(refreshToken)
    }

    private fun getRefreshTokenUser(refreshToken: String): User {
        val userPublicId = authTokenProvider.parse(
            token = refreshToken,
            expectedType = AuthTokenType.REFRESH,
        ).userId

        return try {
            userReader.getUser(userPublicId)
        } catch (exception: UserNotFoundException) {
            throw InvalidAuthTokenException(
                message = "Refresh 토큰의 사용자 정보를 찾을 수 없습니다 [userPublicId=$userPublicId]",
                cause = exception,
            )
        }
    }

}
