package com.jobdori.core.domain.auth.error

import com.jobdori.common.error.ErrorCode

enum class AuthErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    INVALID_OAUTH_AUTHORIZATION_CODE(
        httpStatusCode = 400,
        code = "invalid_authorization_code",
        message = "인증 코드가 올바르지 않습니다.",
        description = "유효하지 않은 OAuth 인증 코드인 경우"
    ),
    ;

}
