package com.jobdori.core.domain.workspace.error

import com.jobdori.common.error.BaseException

data class WorkspaceAccessDeniedException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
    cause = cause,
)
