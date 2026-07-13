package com.jobdori.api.application.jd.dto.response

import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdStatus

data class JdResponse(
    val jdId: String,
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
    val status: JdStatus,
    val createdAt: String?,
) {

    companion object {
        fun from(jd: Jd) = JdResponse(
            jdId = jd.publicId,
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
            status = jd.status,
            createdAt = jd.createdAt?.toString(),
        )
    }

}
