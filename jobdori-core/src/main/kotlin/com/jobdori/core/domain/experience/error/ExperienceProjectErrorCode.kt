package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.ErrorCode

enum class ExperienceProjectErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E400_EXPERIENCE_PROJECT_LIMIT_EXCEEDED(
        httpStatusCode = 400,
        code = "experience_project_limit_exceeded",
        message = "프로젝트는 최대 20개까지 저장할 수 있습니다",
        description = "워크스페이스의 프로젝트 저장 개수가 최대 허용 개수를 초과한 경우",
    ),
    E404_EXPERIENCE_PROJECT_NOT_FOUND(
        httpStatusCode = 404,
        code = "experience_project_not_found",
        message = "존재하지 않는 프로젝트입니다",
        description = "경험 프로젝트를 찾을 수 없는 경우",
    ),
    ;

}
