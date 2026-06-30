package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.ErrorCode

enum class ExperienceProjectErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E404_EXPERIENCE_PROJECT_NOT_FOUND(
        httpStatusCode = 404,
        code = "experience_project_not_found",
        message = "존재하지 않는 프로젝트입니다",
        description = "경험 프로젝트를 찾을 수 없는 경우",
    ),
    ;

}
