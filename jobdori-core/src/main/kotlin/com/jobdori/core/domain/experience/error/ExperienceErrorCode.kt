package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.ErrorCode

enum class ExperienceErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E404_EXPERIENCE_NOT_FOUND(
        httpStatusCode = 404,
        code = "experience_not_found",
        message = "존재하지 않는 경험입니다",
        description = "경험을 찾을 수 없는 경우",
    ),
    ;

}
