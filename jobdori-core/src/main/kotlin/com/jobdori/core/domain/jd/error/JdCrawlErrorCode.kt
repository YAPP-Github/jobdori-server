package com.jobdori.core.domain.jd.error

import com.jobdori.common.error.ErrorCode

enum class JdCrawlErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    E400_JD_INVALID_URL(
        httpStatusCode = 400,
        code = "jd_invalid_url",
        message = "요청할 수 없는 URL입니다.",
        description = "허용되지 않은 스킴이거나 내부 네트워크 주소(SSRF 차단)",
    ),
    E422_JD_ACCESS_DENIED(
        httpStatusCode = 422,
        code = "jd_access_denied",
        message = "해당 페이지에 접근할 수 없습니다. 내용을 직접 붙여넣어 주세요.",
        description = "JD URL 접근 거부(4xx)",
    ),
    E422_JD_FETCH_FAILED(
        httpStatusCode = 422,
        code = "jd_fetch_failed",
        message = "공고 내용을 가져오지 못했습니다. 내용을 직접 붙여넣어 주세요.",
        description = "JD 본문 수집 실패(모든 크롤 단 실패)",
    ),
    ;

}
