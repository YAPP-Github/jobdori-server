package com.jobdori.api.application.auth.dto.request

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.domain.user.UserIdentityProvider
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    val provider: UserIdentityProvider,

    @field:NotBlank
    val authorizationCode: String = "",

    @field:NotBlank
    val redirectUri: String = "",
) {

    fun toCommand(): AuthCommand =
        AuthCommand(
            provider = provider,
            authorizationCode = authorizationCode,
            redirectUri = redirectUri,
        )

}
