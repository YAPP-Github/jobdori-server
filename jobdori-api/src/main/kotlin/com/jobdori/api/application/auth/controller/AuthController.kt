package com.jobdori.api.application.auth.controller

import com.jobdori.api.application.auth.dto.request.LoginRequest
import com.jobdori.api.application.auth.dto.response.AuthTokenResponse
import com.jobdori.api.application.auth.dto.response.LoginResponse
import com.jobdori.api.support.auth.AuthCookieUtils.ACCESS_TOKEN_COOKIE
import com.jobdori.api.support.auth.AuthCookieUtils.REFRESH_TOKEN_COOKIE
import com.jobdori.api.support.auth.AuthCookieUtils.expiredCookie
import com.jobdori.api.support.auth.AuthCookieUtils.tokenCookie
import com.jobdori.api.support.rest.ApiResponse
import com.jobdori.core.application.auth.AuthService
import com.jobdori.core.application.auth.RefreshTokenService
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService,
) {

    @PostMapping("/v1/auth/login")
    fun login(
        @RequestBody @Valid request: LoginRequest,
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val authResult = authService.login(request.toCommand())
        val tokenPair = authResult.tokenPair
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, tokenCookie(ACCESS_TOKEN_COOKIE, tokenPair.accessToken).toString())
            .header(HttpHeaders.SET_COOKIE, tokenCookie(REFRESH_TOKEN_COOKIE, tokenPair.refreshToken).toString())
            .body(ApiResponse.ok(LoginResponse.from(authResult)))
    }

    @PostMapping("/v1/auth/refresh")
    fun refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE) refreshToken: String,
    ): ResponseEntity<ApiResponse<AuthTokenResponse>> {
        val accessToken = refreshTokenService.refresh(refreshToken)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, tokenCookie(ACCESS_TOKEN_COOKIE, accessToken).toString())
            .body(ApiResponse.ok(AuthTokenResponse.from(accessToken)))
    }

    @PostMapping("/v1/auth/logout")
    fun logout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE) refreshToken: String,
    ): ResponseEntity<ApiResponse<Nothing?>> {
        refreshTokenService.validate(refreshToken)
        // TODO: 추후 리프레시 토큰을 저장하고 무효화 시켜야함...
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredCookie(ACCESS_TOKEN_COOKIE).toString())
            .header(HttpHeaders.SET_COOKIE, expiredCookie(REFRESH_TOKEN_COOKIE).toString())
            .body(ApiResponse.OK)
    }

}
