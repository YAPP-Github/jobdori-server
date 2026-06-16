package com.jobdori.core.application.auth.token

import com.jobdori.core.application.auth.error.AuthTokenExpiredException
import com.jobdori.core.application.auth.error.InvalidAuthTokenException
import com.jobdori.core.support.jwt.ExpiredJwtException
import com.jobdori.core.support.jwt.JwtClaims
import com.jobdori.core.support.jwt.JwtCodec
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Component
class JwtAuthTokenProvider(
    private val jwtCodec: JwtCodec,
    private val properties: AuthTokenProperties,
) : AuthTokenProvider {

    override fun issue(userPublicId: String): AuthTokenPair {
        return issue(
            userPublicId = userPublicId,
            accessTokenExpiresAt = null,
            refreshTokenExpiresAt = null,
        )
    }

    override fun issue(
        userPublicId: String,
        accessTokenExpiresAt: Instant?,
        refreshTokenExpiresAt: Instant?,
    ): AuthTokenPair {
        require(userPublicId.isNotBlank()) { "User public ID must not be blank" }

        val issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        accessTokenExpiresAt?.let {
            require(it.isAfter(issuedAt)) { "Access token expiresAt must be after issuedAt" }
        }
        refreshTokenExpiresAt?.let {
            require(it.isAfter(issuedAt)) { "Refresh token expiresAt must be after issuedAt" }
        }

        return AuthTokenPair(
            accessToken = issue(
                userPublicId = userPublicId,
                type = AuthTokenType.ACCESS,
                issuedAt = issuedAt,
                expiresAt = accessTokenExpiresAt ?: issuedAt.plus(properties.accessTokenTtl),
            ),
            refreshToken = issue(
                userPublicId = userPublicId,
                type = AuthTokenType.REFRESH,
                issuedAt = issuedAt,
                expiresAt = refreshTokenExpiresAt ?: issuedAt.plus(properties.refreshTokenTtl),
            ), // TODO: 추후 리프레시 토큰을 저장해서 관리.
        )
    }

    override fun issueAccessToken(userPublicId: String): AuthToken {
        require(userPublicId.isNotBlank()) { "User public ID must not be blank" }
        return issueAccessTokenWithIssuedAt(
            userPublicId = userPublicId,
            issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS),
        )
    }

    override fun issueAccessToken(
        userPublicId: String,
        expiresAt: Instant,
    ): AuthToken {
        require(userPublicId.isNotBlank()) { "User public ID must not be blank" }
        val issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        require(expiresAt.isAfter(issuedAt)) { "Access token expiresAt must be after issuedAt" }

        return issue(
            userPublicId = userPublicId,
            type = AuthTokenType.ACCESS,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
        )
    }

    override fun parse(
        token: String,
        expectedType: AuthTokenType,
    ): AuthTokenPayload {
        val claims = runCatching {
            jwtCodec.decode(token)
        }.getOrElse {
            if (it is ExpiredJwtException) {
                throw AuthTokenExpiredException(
                    message = "인증 토큰이 만료되었습니다",
                    cause = it,
                )
            }
            throw InvalidAuthTokenException(
                message = "인증 토큰을 해석할 수 없습니다",
                cause = it,
            )
        }

        val type = claims.customClaims[TOKEN_TYPE_CLAIM]
            ?.let { runCatching { AuthTokenType.valueOf(it) }.getOrNull() }
            ?: throw InvalidAuthTokenException("인증 토큰 종류가 올바르지 않습니다")

        if (type != expectedType) {
            throw InvalidAuthTokenException(
                "요청에 사용할 수 없는 인증 토큰 종류입니다: expected=$expectedType, actual=$type",
            )
        }

        val userPublicId = claims.subject
            .takeIf(String::isNotBlank)
            ?: throw InvalidAuthTokenException("인증 토큰의 사용자 ID가 올바르지 않습니다")

        return AuthTokenPayload(
            userId = userPublicId,
            tokenId = claims.tokenId,
            type = type,
            issuedAt = claims.issuedAt,
            expiresAt = claims.expiresAt,
        )
    }

    private fun issueAccessTokenWithIssuedAt(
        userPublicId: String,
        issuedAt: Instant,
    ): AuthToken =
        issue(
            userPublicId = userPublicId,
            type = AuthTokenType.ACCESS,
            issuedAt = issuedAt,
            expiresAt = issuedAt.plus(properties.accessTokenTtl),
        )

    private fun issue(
        userPublicId: String,
        type: AuthTokenType,
        issuedAt: Instant,
        expiresAt: Instant,
    ): AuthToken {
        val claims = JwtClaims(
            subject = userPublicId,
            tokenId = UUID.randomUUID().toString(),
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            customClaims = mapOf(TOKEN_TYPE_CLAIM to type.name),
        )

        return AuthToken(
            value = jwtCodec.encode(claims),
            tokenId = claims.tokenId,
            expiresAt = claims.expiresAt,
        )
    }

    private companion object {
        const val TOKEN_TYPE_CLAIM = "token_type"
    }

}
