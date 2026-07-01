package com.jobdori.infrastructure.persistence.ai.repository

import com.jobdori.infrastructure.persistence.ai.entity.AiModelConfigEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AiModelConfigJpaRepository: JpaRepository<AiModelConfigEntity, Long> {
}
