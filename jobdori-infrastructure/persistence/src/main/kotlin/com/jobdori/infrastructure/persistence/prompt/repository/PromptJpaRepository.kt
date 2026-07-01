package com.jobdori.infrastructure.persistence.prompt.repository

import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.infrastructure.persistence.prompt.entity.PromptEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PromptJpaRepository: JpaRepository<PromptEntity, Long> {
    // B안: type당 활성 SYSTEM 행 1개 — 단건 조회
    fun findFirstByTypeAndDeletedAtIsNull(type: PromptType): PromptEntity?
}
