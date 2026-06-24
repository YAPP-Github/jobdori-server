package com.jobdori.core.application.auth.result

import com.jobdori.core.domain.auth.AuthTokenPair

data class AuthResult(
    val tokenPair: AuthTokenPair,
    val isNewUser: Boolean,
)
