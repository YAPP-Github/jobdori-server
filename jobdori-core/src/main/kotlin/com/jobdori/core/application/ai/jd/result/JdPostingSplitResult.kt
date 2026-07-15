package com.jobdori.core.application.ai.jd.result

/**
 * 분할 응답은 본문 텍스트가 아니라 1-based 줄 범위만 받는다.
 * 모델이 원문을 재출력하면 출력 토큰에 비례해 레이턴시가 수십 초로 늘어나기 때문.
 * 실제 본문은 SplitJdPostingsService가 줄 범위로 원문에서 잘라낸다.
 */
data class JdPostingSplitResult(
    val postings: List<PostingRange> = emptyList(),
) {
    data class PostingRange(
        val title: String = "",
        val startLine: Int = 0,
        val endLine: Int = 0,
    )
}

data class JdPosting(
    val title: String = "",
    val body: String = "",
)
