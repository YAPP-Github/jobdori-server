package com.jobdori.core.domain.jd

enum class JdSortType(val comparator: Comparator<Jd>) {
    // 최신순: 등록 역순. createdAt은 null 가능·동시 저장 시 동률이라 단조 증가하는 id로 정렬한다.
    LATEST(compareByDescending { it.id }),

    // 가나다순: 회사명 오름차순(동명은 최신 우선). 추출 실패 시 companyName이 빈 문자열이라 맨 앞에 온다.
    NAME(compareBy<Jd> { it.companyName }.thenByDescending { it.id }),
}
