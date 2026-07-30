package com.jobdori.core.domain.credit.error

import com.jobdori.common.error.ErrorCode

enum class CreditErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E402_INSUFFICIENT_CREDIT(
        httpStatusCode = 402,
        code = "insufficient_credit",
        message = "오늘의 크레딧을 모두 소진했습니다.",
        description = "잔여 크레딧이 0인 상태에서 AI 기능을 호출한 경우",
    ),
    ;

}
