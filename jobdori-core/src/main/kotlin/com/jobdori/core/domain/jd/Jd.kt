package com.jobdori.core.domain.jd

import java.time.LocalDateTime
import java.util.UUID

data class Jd(
    val id: Long,
    val publicId: String,
    val workspaceId: Long,
    val sourceUrl: String?,   // 붙여넣기(text) 등록 시 null
    val sourceBody: String? = null,   // 등록에 쓰인 공고 원문. 원문 조회 화면용. 기능 이전 레거시 행은 null
    val companyName: String,
    val positionTitle: String,
    val companyIntro: String,
    val responsibilities: List<String>,
    val requiredExperiences: List<String>,
    val preferredExperiences: List<String>,
    val hiringProcess: List<String>,
    val coreCompetencies: List<String>,   // AR0001 카드용 핵심 역량 태그(최대 5개). AI 추출 산출물
    val createdAt: LocalDateTime? = null,
) {

    companion object {
        fun newInstance(
            workspaceId: Long,
            sourceUrl: String?,
            sourceBody: String,
            companyName: String,
            positionTitle: String,
            companyIntro: String,
            responsibilities: List<String>,
            requiredExperiences: List<String>,
            preferredExperiences: List<String>,
            hiringProcess: List<String>,
            coreCompetencies: List<String>,
            publicId: String = UUID.randomUUID().toString(),
        ) = Jd(
            id = 0L,
            publicId = publicId,
            workspaceId = workspaceId,
            sourceUrl = sourceUrl,
            sourceBody = sourceBody,
            companyName = companyName,
            positionTitle = positionTitle,
            companyIntro = companyIntro,
            responsibilities = responsibilities,
            requiredExperiences = requiredExperiences,
            preferredExperiences = preferredExperiences,
            hiringProcess = hiringProcess,
            coreCompetencies = coreCompetencies,
        )
    }

}
