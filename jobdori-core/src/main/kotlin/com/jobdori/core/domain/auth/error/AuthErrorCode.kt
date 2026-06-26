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
    OAUTH_ACCESS_DENIED(
        httpStatusCode = 400,
        code = "oauth_access_denied",
        message = "OAuth 인증이 취소되었거나 거부되었습니다.",
        description = "OAuth 인증 또는 동의가 거부된 경우",
    ),
    E409_ALREADY_SIGNED_UP(
        httpStatusCode = 409,
        code = "already_signed_up",
        message = "이미 가입된 계정입니다.",
        description = "이미 가입된 계정으로 회원가입을 시도한 경우",
    ),
    ;

}
