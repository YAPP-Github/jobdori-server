package com.jobdori.core.domain.workspace.error

import com.jobdori.common.error.ErrorCode

enum class WorkspaceErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val description: String,
) : ErrorCode {

    E403_WORKSPACE_ACCESS_DENIED(
        httpStatusCode = 403,
        code = "workspace_access_denied",
        description = "워크스페이스 접근 권한이 없는 경우",
    ),
    E404_WORKSPACE_NOT_FOUND(
        httpStatusCode = 404,
        code = "workspace_not_found",
        description = "워크스페이스를 찾을 수 없는 경우",
    ),
    ;

}
