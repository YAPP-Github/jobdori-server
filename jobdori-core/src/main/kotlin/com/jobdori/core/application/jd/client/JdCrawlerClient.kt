package com.jobdori.core.application.jd.client

/** JD URL에서 본문 텍스트를 수집하는 포트. 실패 시 JdCrawlException. */
interface JdCrawlerClient {
    fun fetchBody(url: String): String
}
