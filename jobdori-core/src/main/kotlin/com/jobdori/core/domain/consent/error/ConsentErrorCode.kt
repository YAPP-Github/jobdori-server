package com.jobdori.core.domain.consent.error

import com.jobdori.common.error.ErrorCode

enum class ConsentErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {
    E409_CONSENT_ALREADY_AGREED(
        httpStatusCode = 409,
        code = "consent_already_agreed",
        message = "이미 가입 동의를 완료했어요.",
        description = "가입 동의 이력이 이미 존재하는 경우",
    ),
}
