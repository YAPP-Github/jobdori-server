package com.jobdori.core.application.ai.jd.result

/** 필드가 data.sql JD_META_EXTRACTION jsonSchema와 1:1 대응한다. */
data class JdMetaResult(
    val isJobPosting: Boolean = true,
    val companyName: String = "",
    val positionTitle: String = "",
    val companyIntro: String = "",
    val responsibilities: List<String> = emptyList(),
    val requiredExperiences: List<String> = emptyList(),
    val preferredExperiences: List<String> = emptyList(),
    val hiringProcess: List<String> = emptyList(),
    val coreCompetencies: List<String> = emptyList(),
    val keyPoints: String = "",
    val strategy: String = "",
) {
    // isJobPosting 오탐 백업: 직무명/업무/필요/우대 경험이 모두 비면 JD로 쓸 실체가 없다.
    fun hasNoJdSubstance(): Boolean =
        positionTitle.isBlank() &&
            responsibilities.isEmpty() &&
            requiredExperiences.isEmpty() &&
            preferredExperiences.isEmpty()
}
