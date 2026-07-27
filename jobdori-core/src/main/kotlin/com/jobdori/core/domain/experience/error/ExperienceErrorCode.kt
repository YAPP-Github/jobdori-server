package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.ErrorCode

enum class ExperienceErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E400_EXPERIENCE_LIMIT_EXCEEDED(
        httpStatusCode = 400,
        code = "experience_limit_exceeded",
        message = "프로젝트별 경험은 최대 10개까지 저장할 수 있습니다",
        description = "프로젝트의 경험 저장 개수가 최대 허용 개수를 초과한 경우",
    ),
    E404_EXPERIENCE_NOT_FOUND(
        httpStatusCode = 404,
        code = "experience_not_found",
        message = "존재하지 않는 경험입니다",
        description = "경험을 찾을 수 없는 경우",
    ),
    E422_EXPERIENCE_REQUIRED(
        httpStatusCode = 422,
        code = "experience_required",
        message = "경험 정리가 필요합니다. 경험을 추가해주세요.",
        description = "워크스페이스에 활성 상태의 경험이 없어 JD를 등록할 수 없는 경우",
    ),
    ;

}
