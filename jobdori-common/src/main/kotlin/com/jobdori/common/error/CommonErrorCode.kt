package com.jobdori.common.error

enum class CommonErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val description: String,
) : ErrorCode {

    E400_INVALID_ARGUMENTS(
        httpStatusCode = 400,
        code = "invalid_arguments",
        description = "필수 파라미터가 없거나, 파라미터가 유효하지 않는 경우",
    ),
    E405_METHOD_NOT_ALLOWED(
        httpStatusCode = 405,
        code = "method_not_allowed",
        description = "허용하지 않는 HTTP Method인 경우",
    ),
    E500_INTERNAL_ERROR(
        httpStatusCode = 500,
        code = "internal_error",
        description = "서버 내부적으로 문제 발생 시",
    ),
    E503_SERVICE_UNAVAILABLE(
        httpStatusCode = 503,
        code = "service_unavailable",
        description = "현재 서비스를 이용할 수 없는 경우",
    ),
    ;

}
