package com.untitled.common.error

enum class ErrorCode(
    val httpStatusCode: Int,
    val code: String,
    val description: String,
) {

    /**
     * 400 BadRequest
     */
    E400_INVALID_ARGUMENTS(
        httpStatusCode = 400,
        code = "invalid_arguments",
        description = "필수 파라미터가 없거나, 파라미터가 유효하지 않는 경우",
    ),

    /**
     * 404 Not Found
     */
    E404_MEMBER_NOT_FOUND(
        httpStatusCode = 404,
        code = "member_not_found",
        description = "회원을 찾을 수 없는 경우",
    ),

    /**
     * 405 Method Not Allowed
     */
    E405_METHOD_NOT_ALLOWED(
        httpStatusCode = 405,
        code = "method_not_allowed",
        description = "허용하지 않는 HTTP Method인 경우",
    ),

    /**
     * 500 Internal Server Error
     */
    E500_INTERNAL_ERROR(
        httpStatusCode = 500,
        code = "internal_error",
        description = "서버 내부적으로 문제 발생 시",
    ),

    /**
     * 503 Service UnAvailable
     */
    E503_SERVICE_UNAVAILABLE(
        httpStatusCode = 503,
        code = "service_unavailable",
        description = "현재 서비스를 이용할 수 없는 경우",
    ),
    ;

}
