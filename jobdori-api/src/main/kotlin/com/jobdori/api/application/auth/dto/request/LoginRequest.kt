package com.jobdori.api.application.auth.dto.request

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.domain.user.UserIdentityProvider
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class LoginRequest(
    val provider: UserIdentityProvider,

    @field:Size(max = 100, message = "입력 가능한 인증 코드의 최대 길이는 {max}자입니다.")
    @field:NotBlank(message = "인증 코드를 입력해 주세요.")
    val authorizationCode: String = "",

    @field:Size(max = 300, message = "입력 가능한 리디렉션 URL의 최대 길이는 {max}자입니다.")
    @field:URL(message = "올바른 리디렉션 URL 형식으로 입력해 주세요.")
    @field:NotBlank(message = "리디렉션 URL을 입력해 주세요.")
    val redirectUri: String = "",
) {

    fun toCommand(): AuthCommand = AuthCommand(
        provider = provider,
        authorizationCode = authorizationCode,
        redirectUri = redirectUri,
    )

}
