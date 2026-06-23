package com.jobdori.api.application.auth.dto.response

import com.jobdori.core.application.auth.result.AuthResult

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
) {

    companion object {
        fun from(authResult: AuthResult): LoginResponse = LoginResponse(
            accessToken = authResult.tokenPair.accessToken.value,
            refreshToken = authResult.tokenPair.refreshToken.value,
            isNewUser = authResult.isNewUser,
        )
    }

}
