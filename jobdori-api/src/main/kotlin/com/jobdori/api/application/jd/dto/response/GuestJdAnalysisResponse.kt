package com.jobdori.api.application.jd.dto.response

import com.jobdori.core.application.jd.GuestJdAnalysisResult
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jdinsight.JdInsight

/** 단일 공고면 analysis, 다중 공고면 candidates 중 하나만 채워진다. */
data class GuestJdAnalysisResponse(
    val analysis: GuestJdAnalysis?,
    val candidates: List<JdCandidateResponse>?,
) {

    companion object {
        fun from(result: GuestJdAnalysisResult): GuestJdAnalysisResponse = when (result) {
            is GuestJdAnalysisResult.Analyzed ->
                GuestJdAnalysisResponse(
                    analysis = GuestJdAnalysis.from(result.jd, result.insight),
                    candidates = null,
                )

            is GuestJdAnalysisResult.MultiplePostings ->
                GuestJdAnalysisResponse(
                    analysis = null,
                    candidates = result.candidates.map { JdCandidateResponse(it.title, it.body) },
                )
        }
    }

}

data class GuestJdAnalysis(
    val sourceUrl: String?,
    val sourceBody: String?,
    val companyName: String,
    val positionTitle: String,
    val companyIntro: String,
    val responsibilities: List<String>,
    val requiredExperiences: List<String>,
    val preferredExperiences: List<String>,
    val hiringProcess: List<String>,
    val coreCompetencies: List<String>,
    val insight: JdInsightResponse,
) {

    companion object {
        fun from(jd: Jd, insight: JdInsight) = GuestJdAnalysis(
            sourceUrl = jd.sourceUrl,
            sourceBody = jd.sourceBody,
            companyName = jd.companyName,
            positionTitle = jd.positionTitle,
            companyIntro = jd.companyIntro,
            responsibilities = jd.responsibilities,
            requiredExperiences = jd.requiredExperiences,
            preferredExperiences = jd.preferredExperiences,
            hiringProcess = jd.hiringProcess,
            coreCompetencies = jd.coreCompetencies,
            insight = JdInsightResponse.from(insight),
        )
    }

}
