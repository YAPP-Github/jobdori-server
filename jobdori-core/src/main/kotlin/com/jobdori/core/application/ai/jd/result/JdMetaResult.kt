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
    // 지원 전략은 JD_APPLICATION_STRATEGY로 분리됨. 이 필드는 미사용이나, 스키마 배포 지연 중 구 응답이 와도
    // 역직렬화가 깨지지 않도록 남겨둔다(Jackson unknown-필드 무시에 의존하지 않기 위함).
    val strategy: String = "",
) {
    // isJobPosting 오탐 백업: 직무명/업무/필요/우대 경험이 모두 비면 JD로 쓸 실체가 없다.
    fun hasNoJdSubstance(): Boolean =
        positionTitle.isBlank() &&
            responsibilities.isEmpty() &&
            requiredExperiences.isEmpty() &&
            preferredExperiences.isEmpty()
}
