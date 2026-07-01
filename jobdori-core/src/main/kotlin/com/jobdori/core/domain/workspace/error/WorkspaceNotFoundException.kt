package com.jobdori.core.domain.workspace.error

import com.jobdori.common.error.BaseException

data class WorkspaceNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
    cause = cause,
)
