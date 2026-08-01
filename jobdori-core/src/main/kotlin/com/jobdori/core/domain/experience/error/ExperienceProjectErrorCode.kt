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
        message = "프로젝트는 20개까지 저장할 수 있어요. 기존 프로젝트를 지운 뒤 다시 저장해 주세요.",
        description = "워크스페이스의 프로젝트 저장 개수가 최대 허용 개수를 초과한 경우",
    ),
    E404_EXPERIENCE_PROJECT_NOT_FOUND(
        httpStatusCode = 404,
        code = "experience_project_not_found",
        message = "프로젝트를 찾지 못했어요. 목록에서 다시 확인해 주세요.",
        description = "경험 프로젝트를 찾을 수 없는 경우",
    ),
    ;

}
