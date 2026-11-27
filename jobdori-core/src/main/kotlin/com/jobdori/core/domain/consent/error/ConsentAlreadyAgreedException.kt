package com.jobdori.core.domain.consent.error

import com.jobdori.common.error.BaseException

data class ConsentAlreadyAgreedException(
    override val message: String = ConsentErrorCode.E409_CONSENT_ALREADY_AGREED.message,
    override val errorCode: ConsentErrorCode = ConsentErrorCode.E409_CONSENT_ALREADY_AGREED,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = errorCode, cause = cause)
