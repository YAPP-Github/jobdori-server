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
        message = "경험은 프로젝트마다 10개까지 저장할 수 있어요. 기존 경험을 지운 뒤 다시 저장해 주세요.",
        description = "프로젝트의 경험 저장 개수가 최대 허용 개수를 초과한 경우",
    ),
    E404_EXPERIENCE_NOT_FOUND(
        httpStatusCode = 404,
        code = "experience_not_found",
        message = "경험을 찾지 못했어요. 목록에서 다시 확인해 주세요.",
        description = "경험을 찾을 수 없는 경우",
    ),
    E422_EXPERIENCE_REQUIRED(
        httpStatusCode = 422,
        code = "experience_required",
        message = "이력서 생성을 위한 경험이 필요해요. 내 경험 탭에서 경험을 추가해 주세요.",
        description = "워크스페이스에 활성 상태의 경험이 없어 JD를 등록할 수 없는 경우",
    ),
    ;

}
