package com.jobdori.api.application.auth.controller

import com.jobdori.api.application.auth.dto.response.AuthTokenResponse
import com.jobdori.api.support.auth.AuthCookieUtils.ACCESS_TOKEN_COOKIE
import com.jobdori.api.support.auth.AuthCookieUtils.REFRESH_TOKEN_COOKIE
import com.jobdori.api.support.auth.AuthCookieUtils.tokenCookie
import com.jobdori.api.support.rest.ApiResponse
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.domain.auth.service.AuthTokenProvider
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.repository.UserRepository
import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.repository.WorkspaceRepository
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId

@Profile("local", "dev", "test")
@RestController
class TestTokenController(
    private val userRepository: UserRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val authTokenProvider: AuthTokenProvider,
) {

    @GetMapping("/test-tokens")
    fun issueTestToken(
        @RequestParam(required = false) accessTokenExpiresAt: LocalDateTime?,
        @RequestParam(required = false) refreshTokenExpiresAt: LocalDateTime?,
    ): ResponseEntity<ApiResponse<AuthTokenResponse>> {
        validateExpiresAt(
            accessTokenExpiresAt = accessTokenExpiresAt,
            refreshTokenExpiresAt = refreshTokenExpiresAt,
        )

        val user = userRepository.findByPublicId(TEST_USER_PUBLIC_ID)
            ?: userRepository.save(
                User.newInstance(
                    publicId = TEST_USER_PUBLIC_ID,
                    email = TEST_USER_EMAIL,
                    name = "잡도리",
                    profileImageUrl = null,
                ),
            )
        ensureTestWorkspace(user)

        val tokenPair = authTokenProvider.issue(
            userPublicId = user.publicId,
            accessTokenExpiresAt = accessTokenExpiresAt?.atZone(ZoneId.systemDefault())?.toInstant(),
            refreshTokenExpiresAt = refreshTokenExpiresAt?.atZone(ZoneId.systemDefault())?.toInstant(),
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, tokenCookie(ACCESS_TOKEN_COOKIE, tokenPair.accessToken).toString())
            .header(HttpHeaders.SET_COOKIE, tokenCookie(REFRESH_TOKEN_COOKIE, tokenPair.refreshToken).toString())
            .body(ApiResponse.ok(AuthTokenResponse.from(tokenPair)))
    }

    private fun ensureTestWorkspace(user: User) {
        if (workspaceRepository.findAllByOwnerUserId(user.id).isNotEmpty()) {
            return
        }
        workspaceRepository.save(Workspace.newInstance(ownerUserId = user.id))
    }

    private fun validateExpiresAt(
        accessTokenExpiresAt: LocalDateTime?,
        refreshTokenExpiresAt: LocalDateTime?,
    ) {
        val now = LocalDateTime.now()

        if (accessTokenExpiresAt != null && !accessTokenExpiresAt.isAfter(now)) {
            throw InvalidArgumentsException("Access 토큰 만료 시각은 현재 시각 이후여야 합니다")
        }
        if (refreshTokenExpiresAt != null && !refreshTokenExpiresAt.isAfter(now)) {
            throw InvalidArgumentsException("Refresh 토큰 만료 시각은 현재 시각 이후여야 합니다")
        }
    }

    private companion object {
        const val TEST_USER_PUBLIC_ID = "00000000-0000-0000-0000-000000000001"
        const val TEST_USER_EMAIL = "test@jobdori.com"
    }

}
