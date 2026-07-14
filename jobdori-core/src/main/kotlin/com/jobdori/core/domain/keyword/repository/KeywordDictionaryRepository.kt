package com.jobdori.core.domain.keyword.repository

import com.jobdori.core.domain.keyword.KeywordType

// 사용자 간 공유되는 키워드 사전. 프로필 항목은 문자열을 저장하고 사전은 자동완성 제안에만 쓴다
// (사전 수정/삭제가 기존 프로필 데이터에 영향을 주지 않도록 FK로 참조하지 않는다)
interface KeywordDictionaryRepository {

    fun searchNames(type: KeywordType, keyword: String, size: Int): List<String>

    fun registerAll(type: KeywordType, names: Collection<String>)

}
