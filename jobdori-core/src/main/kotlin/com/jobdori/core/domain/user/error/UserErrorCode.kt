package com.jobdori.core.domain.user.error

import com.jobdori.common.error.ErrorCode

enum class UserErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val description: String,
) : ErrorCode {

    E404_USER_NOT_FOUND(
        httpStatusCode = 404,
        code = "user_not_found",
        description = "유저를 찾을 수 없는 경우",
    ),
    E409_USER_ALREADY_EXISTS(
        httpStatusCode = 409,
        code = "user_already_exists",
        description = "이미 가입한 유저인 경우",
    ),
    ;

}
