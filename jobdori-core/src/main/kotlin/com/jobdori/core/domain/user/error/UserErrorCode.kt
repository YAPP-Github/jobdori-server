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
        description = "사용자를 찾을 수 없는 경우",
    ),
    ;

}
