package com.jobdori.core.application.ai.jd.result

/** 필드가 data.sql JD_META_EXTRACTION jsonSchema와 1:1 대응한다. */
data class JdMetaResult(
    val companyName: String = "",
    val positionTitle: String = "",
    val companyIntro: String = "",
    val responsibilities: List<String> = emptyList(),
    val requiredExperiences: List<String> = emptyList(),
    val preferredExperiences: List<String> = emptyList(),
    val hiringProcess: List<String> = emptyList(),
)
