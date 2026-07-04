package com.jobdori.core.application.jd

import com.jobdori.core.application.ai.jd.result.JdPosting
import com.jobdori.core.domain.jd.Jd

sealed interface JdRegisterResult {
    data class Registered(val jd: Jd) : JdRegisterResult

    /** 미저장 후보 — 하나를 골라 body로 재등록한다. */
    data class MultiplePostings(val candidates: List<JdPosting>) : JdRegisterResult
}
