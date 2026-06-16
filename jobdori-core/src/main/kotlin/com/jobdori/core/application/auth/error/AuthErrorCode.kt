package com.jobdori.core.application.auth.error

import com.jobdori.common.error.ErrorCode

enum class AuthErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val description: String,
) : ErrorCode {

    E409_ALREADY_SIGNED_UP(
        httpStatusCode = 409,
        code = "already_signed_up",
        description = "이미 가입된 계정으로 회원가입을 시도한 경우",
    ),
    ;

}
