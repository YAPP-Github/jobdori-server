package com.jobdori.core.domain.workspace.error

import com.jobdori.common.error.ErrorCode

enum class WorkspaceErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E403_WORKSPACE_ACCESS_DENIED(
        httpStatusCode = 403,
        code = "workspace_access_denied",
        message = "이 워크스페이스에 접근할 권한이 없어요.",
        description = "워크스페이스 접근 권한이 없는 경우",
    ),
    E404_WORKSPACE_NOT_FOUND(
        httpStatusCode = 404,
        code = "workspace_not_found",
        message = "워크스페이스를 찾지 못했어요. 목록에서 다시 확인해 주세요.",
        description = "워크스페이스를 찾을 수 없는 경우",
    ),
    ;

}
