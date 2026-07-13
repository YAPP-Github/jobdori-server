package com.jobdori.core.domain.jd

// AR0001 아카이브 섹션 구분. 이력서 완료 여부를 JD가 들고 있고(JD->이력서), Resume가 완료 시 flip한다.
enum class JdStatus {
    IN_PROGRESS,   // 등록 직후 기본. 이력서 미완성 -> '진행 중' 섹션
    COMPLETED,     // 연결된 이력서 생성 완료 -> '완료' 섹션
}
