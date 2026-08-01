package com.jobdori.core.domain.resume.error

import com.jobdori.common.error.ErrorCode

enum class ResumeErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E404_RESUME_NOT_FOUND(
        httpStatusCode = 404,
        code = "resume_not_found",
        message = "이력서를 찾지 못했어요. 목록에서 다시 확인해 주세요.",
        description = "이력서를 찾을 수 없는 경우",
    ),
    ;

}
