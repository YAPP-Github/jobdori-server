package com.jobdori.core.domain.prompt

enum class PromptType {
    JD_MULTI_POSTING_SPLIT,        // jd 등록 시 다중 공고 분할
    JD_META_EXTRACTION,            // JD 메타(기업명, 포지션, 소개, 업무, 필요/우대경험, 전형절차) 추출
    EXPERIENCE_RECOMMENDATION,     // jd-경험 매칭률(전량) + 상위 이유(구조화)
    EXPERIENCE_STAR_EXTRACTION,    // 경험 STAR 추출
    RESUME_EXPERIENCE_REWRITE,     // 이력서 문장 생성
    EXPERIENCE_CONTENTS_POLISH,    // Free Style 경험 내용을 STAR로 변환
    PROFILE_CORE_COMPETENCY_GENERATION, // 프로필(이력서 기본 정보) 기반 핵심역량 생성
    PROFILE_TEXT_POLISH,           // 프로필 텍스트(핵심역량/경력 세부/경험명/STAR) 다듬기
}
