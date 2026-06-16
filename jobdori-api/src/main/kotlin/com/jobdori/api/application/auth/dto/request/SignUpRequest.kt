package com.jobdori.api.application.auth.dto.request

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.domain.user.UserIdentifyProvider
import jakarta.validation.constraints.NotBlank

data class SignUpRequest(
    val provider: UserIdentifyProvider,

    @field:NotBlank
    val authorizationCode: String = "",
) {

    fun toCommand(): AuthCommand =
        AuthCommand(
            provider = provider,
            authorizationCode = authorizationCode,
        )

}
