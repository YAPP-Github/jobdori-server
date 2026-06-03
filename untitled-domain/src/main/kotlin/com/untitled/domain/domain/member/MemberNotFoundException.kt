package com.untitled.domain.domain.member

import com.untitled.common.error.BaseException
import com.untitled.common.error.ErrorCode

data class MemberNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ErrorCode.E404_MEMBER_NOT_FOUND,
    cause = cause,
)
