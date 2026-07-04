package com.jobdori.infrastructure.client.jd

import com.jobdori.core.domain.jd.JdPolicy
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jd.crawler")
data class JdCrawlerProperties(
    val userAgent: String = "Mozilla/5.0 (compatible; JobdoriBot/1.0)",
    val minBodyLength: Int = JdPolicy.MIN_JD_BODY_LENGTH,   // 이 미만이면 수집 실패 → 붙여넣기 폴백
    val maxBodyBytes: Int = 3_000_000,                      // 응답 본문 상한(메모리·DoS 방어). 초과분은 읽지 않음
)
