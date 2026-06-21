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
    E401_INVALID_AUTH_TOKEN(
        httpStatusCode = 401,
        code = "invalid_auth_token",
        description = "인증 토큰이 없거나 유효하지 않은 경우",
    ),
    E401_TOKEN_EXPIRED(
        httpStatusCode = 401,
        code = "token_expired",
        description = "인증 토큰이 만료된 경우 (액세스 토큰 갱신 필요)",
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
