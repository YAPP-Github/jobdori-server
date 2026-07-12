package com.jobdori.core.application.jd

import com.jobdori.core.application.ai.jd.result.JdPosting
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jdinsight.JdInsight

sealed interface GuestJdAnalysisResult {
    /** 단일 공고 - 저장하지 않은 분석 결과와 인사이트. */
    data class Analyzed(val jd: Jd, val insight: JdInsight) : GuestJdAnalysisResult

    /** 다중 공고 - 하나를 골라 body로 재분석한다. */
    data class MultiplePostings(val candidates: List<JdPosting>) : GuestJdAnalysisResult
}
