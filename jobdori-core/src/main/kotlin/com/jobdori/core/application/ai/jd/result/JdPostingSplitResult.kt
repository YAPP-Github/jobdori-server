package com.jobdori.core.application.ai.jd.result

data class JdPostingSplitResult(
    val postings: List<JdPosting> = emptyList(),
)

data class JdPosting(
    val title: String = "",
    val body: String = "",
)
