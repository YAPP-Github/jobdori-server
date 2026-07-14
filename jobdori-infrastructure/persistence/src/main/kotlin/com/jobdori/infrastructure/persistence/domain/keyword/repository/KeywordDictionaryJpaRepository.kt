package com.jobdori.infrastructure.persistence.domain.keyword.repository

import com.jobdori.core.domain.keyword.KeywordType
import com.jobdori.infrastructure.persistence.domain.keyword.entity.KeywordDictionaryEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface KeywordDictionaryJpaRepository : JpaRepository<KeywordDictionaryEntity, Long> {

    @Query(
        """
        SELECT k FROM KeywordDictionaryEntity k
        WHERE k.type = :type
          AND LOWER(k.name) LIKE :keywordPattern ESCAPE '\'
        ORDER BY k.name
        """,
    )
    fun searchAllByTypeAndName(
        @Param("type") type: KeywordType,
        @Param("keywordPattern") keywordPattern: String,
        pageable: Pageable,
    ): List<KeywordDictionaryEntity>

    fun findAllByTypeAndNameIn(type: KeywordType, names: Collection<String>): List<KeywordDictionaryEntity>

}
