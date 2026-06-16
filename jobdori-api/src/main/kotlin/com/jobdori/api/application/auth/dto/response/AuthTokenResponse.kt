package com.jobdori.api.application.auth.dto.response

import com.jobdori.core.domain.auth.AuthToken
import com.jobdori.core.domain.auth.AuthTokenPair

data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
) {

    companion object {
        fun from(accessToken: AuthToken): AuthTokenResponse = AuthTokenResponse(
            accessToken = accessToken.value,
        )

        fun from(tokenPair: AuthTokenPair): AuthTokenResponse = AuthTokenResponse(
            accessToken = tokenPair.accessToken.value,
            refreshToken = tokenPair.refreshToken.value,
        )
    }

}
