package com.jobdori.core.domain.keyword.service

import com.jobdori.core.domain.keyword.KeywordType
import com.jobdori.core.domain.keyword.repository.KeywordDictionaryRepository
import org.springframework.stereotype.Service

@Service
class KeywordRegistrar(
    private val keywordDictionaryRepository: KeywordDictionaryRepository,
) {

    fun register(type: KeywordType, names: Collection<String?>) {
        val normalized = names.asSequence()
            .filterNotNull()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
        if (normalized.isEmpty()) {
            return
        }

        keywordDictionaryRepository.registerAll(type = type, names = normalized)
    }

}
