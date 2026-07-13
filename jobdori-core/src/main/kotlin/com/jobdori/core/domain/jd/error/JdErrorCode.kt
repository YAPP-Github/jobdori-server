package com.jobdori.core.domain.jd.error

import com.jobdori.common.error.ErrorCode

enum class JdErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E404_JD_NOT_FOUND(
        httpStatusCode = 404,
        code = "jd_not_found",
        message = "등록되지 않은 JD입니다.",
        description = "존재하지 않거나 소유자가 아닌 JD 조회",
    ),
    ;

}
