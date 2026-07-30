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
        message = "링크 형식이 올바르지 않아요. 확인 후 다시 시도해 주세요.",
        description = "허용되지 않은 스킴이거나 내부 네트워크 주소(SSRF 차단)",
    ),
    E422_JD_ACCESS_DENIED(
        httpStatusCode = 422,
        code = "jd_access_denied",
        message = "이 페이지에 접근할 수 없어요. 원문을 직접 복사해 붙여 넣어 주세요.",
        description = "JD URL 접근 거부(4xx)",
    ),
    E422_JD_FETCH_FAILED(
        httpStatusCode = 422,
        code = "jd_fetch_failed",
        message = "내용을 불러올 수 없어요. 원문을 직접 복사해 붙여 넣어 주세요.",
        description = "JD 본문 수집 실패(모든 크롤 단 실패)",
    ),
    E422_JD_NOT_A_POSTING(
        httpStatusCode = 422,
        code = "jd_not_a_posting",
        message = "채용공고가 아닌 것 같아요. 채용공고 주소나 내용을 확인해 주세요.",
        description = "AI가 채용 공고로 인식하지 못한 본문/URL",
    ),
    ;

}
