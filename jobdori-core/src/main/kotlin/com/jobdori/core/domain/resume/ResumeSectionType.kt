package com.jobdori.core.domain.resume

enum class ResumeSectionType(
    val displayText: String,
    val defaultDisplayOrder: Double,
) {

    BASIC_INFO(
        displayText = "기본 정보",
        defaultDisplayOrder = 10.0,
    ),
    CORE_SKILL(
        displayText = "핵심 역량",
        defaultDisplayOrder = 20.0,
    ),
    EXPERIENCE(
        displayText = "경험",
        defaultDisplayOrder = 30.0,
    ),
    CAREER(
        displayText = "경력",
        defaultDisplayOrder = 40.0,
    ),
    EDUCATION(
        displayText = "학력",
        defaultDisplayOrder = 60.0,
    ),
    LANGUAGE(
        displayText = "어학",
        defaultDisplayOrder = 80.0,
    ),
    AWARD(
        displayText = "수상",
        defaultDisplayOrder = 70.0,
    ),
    CERTIFICATE(
        displayText = "자격증",
        defaultDisplayOrder = 90.0,
    ),
    SKILL(
        displayText = "기술",
        defaultDisplayOrder = 100.0,
    ),

}
