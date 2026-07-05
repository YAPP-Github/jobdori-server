package com.jobdori.core.domain.prompt

enum class PromptType {
    JD_MULTI_POSTING_SPLIT,        // jd 등록 시 다중 공고 분할
    JD_META_EXTRACTION,            // jd 기업명·직무명·정제본문·역량태그 추출
    JD_APPLICATION_STRATEGY,       // jd 지원 전략 생성(서술형)
    EXPERIENCE_STAR_EXTRACTION,    // 경험 STAR 추출
    RESUME_EXPERIENCE_REWRITE,     // 이력서 문장 생성
    EXPERIENCE_CONTENTS_POLISH,    // Free Style 경험 내용을 STAR로 변환
}
