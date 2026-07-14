package com.jobdori.core.domain.keyword.service

import com.jobdori.core.domain.keyword.KeywordType
import com.jobdori.core.domain.keyword.repository.KeywordDictionaryRepository
import org.springframework.stereotype.Service

@Service
class KeywordReader(
    private val keywordDictionaryRepository: KeywordDictionaryRepository,
) {

    fun suggest(type: KeywordType, keyword: String, size: Int): List<String> {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) {
            return emptyList()
        }

        return keywordDictionaryRepository.searchNames(type = type, keyword = trimmed, size = size)
    }

}
