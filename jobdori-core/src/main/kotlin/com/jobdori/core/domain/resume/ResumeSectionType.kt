package com.jobdori.core.domain.resume

import java.math.BigDecimal

enum class ResumeSectionType(
    val displayText: String,
    val defaultDisplayOrder: BigDecimal,
) {

    BASIC_INFO(
        displayText = "기본 정보",
        defaultDisplayOrder = BigDecimal("10"),
    ),
    CORE_SKILL(
        displayText = "핵심 역량",
        defaultDisplayOrder = BigDecimal("20"),
    ),
    EXPERIENCE(
        displayText = "경험",
        defaultDisplayOrder = BigDecimal("30"),
    ),
    CAREER(
        displayText = "경력",
        defaultDisplayOrder = BigDecimal("40"),
    ),
    EDUCATION(
        displayText = "학력",
        defaultDisplayOrder = BigDecimal("60"),
    ),
    LANGUAGE(
        displayText = "어학",
        defaultDisplayOrder = BigDecimal("80"),
    ),
    AWARD(
        displayText = "수상",
        defaultDisplayOrder = BigDecimal("70"),
    ),
    CERTIFICATE(
        displayText = "자격증",
        defaultDisplayOrder = BigDecimal("90"),
    ),
    SKILL(
        displayText = "기술",
        defaultDisplayOrder = BigDecimal("100"),
    ),

}
