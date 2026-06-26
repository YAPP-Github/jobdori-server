package com.jobdori.api.application.auth.dto.request

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.domain.user.UserIdentityProvider
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class LoginRequest(
    val provider: UserIdentityProvider,

    @field:Size(max = 100)
    @field:NotBlank
    val authorizationCode: String = "",

    @field:Size(max = 300)
    @field:URL
    @field:NotBlank
    val redirectUri: String = "",
) {

    fun toCommand(): AuthCommand = AuthCommand(
        provider = provider,
        authorizationCode = authorizationCode,
        redirectUri = redirectUri,
    )

}
