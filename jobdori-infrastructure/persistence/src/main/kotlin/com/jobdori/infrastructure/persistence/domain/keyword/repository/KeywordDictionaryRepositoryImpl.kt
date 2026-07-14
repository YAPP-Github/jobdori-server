package com.jobdori.infrastructure.persistence.domain.keyword.repository

import com.jobdori.core.domain.keyword.KeywordType
import com.jobdori.core.domain.keyword.repository.KeywordDictionaryRepository
import com.jobdori.infrastructure.persistence.domain.keyword.entity.KeywordDictionaryEntity
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class KeywordDictionaryRepositoryImpl(
    private val jpaRepository: KeywordDictionaryJpaRepository,
) : KeywordDictionaryRepository {

    @Transactional(readOnly = true)
    override fun searchNames(type: KeywordType, keyword: String, size: Int): List<String> {
        val escapedKeyword = escapeLikeWildcards(keyword.lowercase())

        return jpaRepository.searchAllByTypeAndName(
            type = type,
            keywordPattern = "%${escapedKeyword}%",
            pageable = PageRequest.of(0, size),
        ).map { it.name }
    }

    @Transactional
    override fun registerAll(type: KeywordType, names: Collection<String>) {
        val existing = jpaRepository.findAllByTypeAndNameIn(type, names).map { it.name }.toSet()
        val newEntities = names
            .filterNot { it in existing }
            .map { KeywordDictionaryEntity(type = type, name = it) }
        if (newEntities.isEmpty()) {
            return
        }

        jpaRepository.saveAll(newEntities)
    }

    private fun escapeLikeWildcards(keyword: String): String {
        return keyword
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
    }

}
