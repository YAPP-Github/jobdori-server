package com.untitled.api.support.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.untitled.common.error.ErrorDetail
import com.untitled.common.error.ErrorCode

data class ApiResponse<T>(
    val ok: Boolean,
    val error: ApiError? = null,
    val result: T?,
) {

    data class ApiError(
        val code: String,
        @field:JsonInclude(JsonInclude.Include.NON_EMPTY)
        val details: List<ErrorDetail> = emptyList(),
    )

    companion object {
        fun <T> ok(result: T): ApiResponse<T> =
            ApiResponse(ok = true, result = result)

        fun <T> fail(
            error: ErrorCode,
            details: List<ErrorDetail> = emptyList(),
        ): ApiResponse<T> = ApiResponse(
            ok = false,
            error = ApiError(
                code = error.code,
                details = details,
            ),
            result = null,
        )

        val OK: ApiResponse<Nothing?> = ok(result = null)
    }

}
