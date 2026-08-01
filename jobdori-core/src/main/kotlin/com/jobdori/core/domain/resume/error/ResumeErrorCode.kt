package com.jobdori.core.domain.resume.error

import com.jobdori.common.error.ErrorCode

enum class ResumeErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E400_RESUME_SECTION_ITEM_REQUIRED(
        httpStatusCode = 400,
        code = "resume_section_item_required",
        message = "빈 섹션이 있어 저장할 수 없어요. 항목을 추가하거나 빈 섹션을 지워 주세요.",
        description = "기본 아이템을 사용하지 않는 이력서 섹션에 아이템이 없는 경우",
    ),
    E404_RESUME_NOT_FOUND(
        httpStatusCode = 404,
        code = "resume_not_found",
        message = "이력서를 찾지 못했어요. 목록에서 다시 확인해 주세요.",
        description = "이력서를 찾을 수 없는 경우",
    ),
    ;

}
