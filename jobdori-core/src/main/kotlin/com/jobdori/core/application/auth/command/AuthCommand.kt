package com.jobdori.core.application.auth.command

import com.jobdori.core.domain.user.UserIdentifyProvider

data class AuthCommand(
    val provider: UserIdentifyProvider,
    val authorizationCode: String,
)
