package com.jobdori.core.domain.jd

import java.time.LocalDateTime
import java.util.UUID

data class Jd(
    val id: Long,
    val publicId: String,
    val workspaceId: Long,
    val sourceUrl: String?,   // 붙여넣기(text) 등록 시 null
    val companyName: String,
    val positionTitle: String,
    val companyIntro: String,
    val responsibilities: List<String>,
    val requiredExperiences: List<String>,
    val preferredExperiences: List<String>,
    val hiringProcess: List<String>,
    val createdAt: LocalDateTime? = null,
) {

    companion object {
        fun newInstance(
            workspaceId: Long,
            sourceUrl: String?,
            companyName: String,
            positionTitle: String,
            companyIntro: String,
            responsibilities: List<String>,
            requiredExperiences: List<String>,
            preferredExperiences: List<String>,
            hiringProcess: List<String>,
            publicId: String = UUID.randomUUID().toString(),
        ) = Jd(
            id = 0L,
            publicId = publicId,
            workspaceId = workspaceId,
            sourceUrl = sourceUrl,
            companyName = companyName,
            positionTitle = positionTitle,
            companyIntro = companyIntro,
            responsibilities = responsibilities,
            requiredExperiences = requiredExperiences,
            preferredExperiences = preferredExperiences,
            hiringProcess = hiringProcess,
        )
    }

}
