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
        message = "파일이 허용 용량보다 커요. 용량를 줄여 다시 업로드해 주세요.",
        description = "업로드한 파일 크기가 허용된 최대 크기를 초과한 경우",
    ),
    ;

}
