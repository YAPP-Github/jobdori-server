package com.jobdori.core.domain.credit

enum class CreditFeature(
    val cost: Int,
) {

    JD_ANALYSIS(5),
    RESUME_DRAFT(5),
    AI_POLISH(1),
    EXPERIENCE_REWRITE(1),
    EXPERIENCE_IMPORT(10),
    EXPERIENCE_EXTRACT(1),
    ;

}
