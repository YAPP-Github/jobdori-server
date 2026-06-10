package com.jobdori.core.domain.sample

/**
 * 샘플 이름 값 객체(Value Object).
 *
 * 식별자가 없고 값으로 동등성을 가지며, 생성 시점에 이름 규칙(불변식)을 스스로 보장한다.
 * 검증을 VO에 캡슐화하여 잘못된 이름이 도메인에 존재할 수 없게 한다.
 */
@JvmInline
value class SampleName(val value: String) {

    init {
        require(value.isNotBlank()) { "샘플 이름은 비어 있을 수 없습니다" }
        require(value.length <= MAX_LENGTH) { "샘플 이름은 ${MAX_LENGTH}자 이하여야 합니다" }
    }

    companion object {
        const val MAX_LENGTH = 50
    }

}
