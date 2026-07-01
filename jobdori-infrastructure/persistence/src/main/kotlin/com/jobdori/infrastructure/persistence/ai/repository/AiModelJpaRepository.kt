package com.jobdori.infrastructure.persistence.ai.repository

import com.jobdori.infrastructure.persistence.ai.entity.AiModelEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AiModelJpaRepository: JpaRepository<AiModelEntity, Long> {
}
