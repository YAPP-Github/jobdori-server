package com.jobdori.core.domain.sample.model

import com.jobdori.core.domain.sample.vo.SampleName

data class Sample(
    val id: Long,
    val name: SampleName,
) {

    /**
     * 샘플 이름을 변경한다. 이름 검증 규칙은 [SampleName] VO가 책임지므로,
     * 도메인 모델은 새 값 객체로 교체된 불변 인스턴스를 반환하기만 한다.
     */
    fun rename(newName: String): Sample {
        return copy(name = SampleName(newName))
    }

}
