package com.jobdori.core.application.auth.command

import com.jobdori.core.domain.user.UserIdentityProvider

data class AuthCommand(
    val provider: UserIdentityProvider,
    val authorizationCode: String,
    val redirectUri: String,
)
