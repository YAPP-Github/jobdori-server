package com.jobdori.core.domain.prompt

enum class PromptType {
    JD_MULTI_POSTING_SPLIT,        // jd 등록 시 다중 공고 분할
    JD_META_EXTRACTION,            // JD 메타(기업명, 포지션, 소개, 업무, 필요/우대경험, 전형절차) 추출
    JD_KEY_POINTS,                 // jd 공고 핵심(인재상·요구) 요약(서술형)
    JD_APPLICATION_STRATEGY,       // jd 지원 전략 생성(서술형)
    EXPERIENCE_RECOMMENDATION,     // jd-경험 매칭률(전량) + 상위 이유(구조화)
    EXPERIENCE_STAR_EXTRACTION,    // 경험 STAR 추출
    RESUME_EXPERIENCE_REWRITE,     // 이력서 문장 생성
}
