package com.jobdori.core.domain.credit.error

import com.jobdori.common.error.BaseException

data class InsufficientCreditException(
    override val message: String = CreditErrorCode.E402_INSUFFICIENT_CREDIT.message,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = CreditErrorCode.E402_INSUFFICIENT_CREDIT,
    cause = cause,
)
