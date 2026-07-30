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
        message = "로그인에 실패했어요. 처음부터 다시 로그인해 주세요.",
        description = "유효하지 않은 OAuth 인증 코드인 경우"
    ),
    ;

}
