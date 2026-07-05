package com.jobdori.common.error

enum class FileErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E400_FILE_SIZE_EXCEEDED(
        httpStatusCode = 400,
        code = "file_size_exceeded",
        message = "파일 크기 제한을 초과했습니다.",
        description = "업로드한 파일 크기가 허용된 최대 크기를 초과한 경우",
    ),
    ;

}
