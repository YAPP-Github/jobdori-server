package com.jobdori.common.error

enum class CommonErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E400_INVALID_ARGUMENTS(
        httpStatusCode = 400,
        code = "invalid_arguments",
        message = "입력한 내용을 다시 확인해 주세요.",
        description = "필수 파라미터가 없거나, 파라미터가 유효하지 않는 경우",
    ),
    E401_INVALID_AUTH_TOKEN(
        httpStatusCode = 401,
        code = "invalid_auth_token",
        message = "로그인이 필요해요. 로그인 후 다시 시도해 주세요.",
        description = "인증 토큰이 없거나 유효하지 않은 경우",
    ),
    E401_TOKEN_EXPIRED(
        httpStatusCode = 401,
        code = "token_expired",
        message = "로그인이 만료됐어요. 다시 로그인해 주세요.",
        description = "인증 토큰이 만료된 경우 (액세스 토큰 갱신 필요)",
    ),
    E500_INTERNAL_ERROR(
        httpStatusCode = 500,
        code = "internal_error",
        message = "일시적인 오류가 생겼어요. 잠시 후 다시 시도해 주세요.",
        description = "서버 내부적으로 문제 발생 시",
    ),
    E503_SERVICE_UNAVAILABLE(
        httpStatusCode = 503,
        code = "service_unavailable",
        message = "지금은 서비스를 이용할 수 없어요. 잠시 후 다시 시도해 주세요.",
        description = "현재 서비스를 이용할 수 없는 경우",
    ),
    ;

}
