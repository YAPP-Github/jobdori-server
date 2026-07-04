package com.jobdori.api.application.jd.dto.response

import com.jobdori.core.application.jd.JdRegisterResult

/** 단일 공고면 jd, 다중 공고면 candidates 중 하나만 채워진다. */
data class JdRegisterResponse(
    val jd: JdResponse?,
    val candidates: List<JdCandidateResponse>?,
) {

    companion object {
        fun from(result: JdRegisterResult): JdRegisterResponse = when (result) {
            is JdRegisterResult.Registered ->
                JdRegisterResponse(jd = JdResponse.from(result.jd), candidates = null)

            is JdRegisterResult.MultiplePostings ->
                JdRegisterResponse(
                    jd = null,
                    candidates = result.candidates.map { JdCandidateResponse(it.title, it.body) },
                )
        }
    }

}

data class JdCandidateResponse(
    val title: String,
    val body: String,
)
