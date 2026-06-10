package com.jobdori.core.domain.sample.error

import com.jobdori.common.error.ErrorCode

enum class SampleErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val description: String,
) : ErrorCode {

    E404_SAMPLE_NOT_FOUND(
        httpStatusCode = 404,
        code = "sample_not_found",
        description = "샘플을 찾을 수 없는 경우",
    ),
    ;

}
