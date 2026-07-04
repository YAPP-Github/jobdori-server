package com.jobdori.core.domain.jd

/** JD 입력 정책 단일 출처(RegisterJd·ExtractJdMeta 공유) */
object JdPolicy {
    const val MIN_JD_BODY_LENGTH = 500     // HM0002 붙여넣기 본문 최소 길이
    const val MAX_JD_LENGTH = 10_000       // JD 본문 최대 길이
    const val MAX_SPLIT_CANDIDATES = 6     // HM0003 다중 JD 후보 최대 노출 개수
}
