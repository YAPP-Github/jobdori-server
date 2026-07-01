package com.jobdori.infrastructure.persistence.prompt.repository

import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import com.jobdori.infrastructure.persistence.ai.repository.AiModelConfigJpaRepository
import com.jobdori.infrastructure.persistence.ai.repository.AiModelJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class PromptTemplateRepositoryImpl(
    private val promptJpaRepository: PromptJpaRepository,
    private val configJpaRepository: AiModelConfigJpaRepository,
    private val modelJpaRepository: AiModelJpaRepository,
): PromptTemplateRepository {
    override fun findByType(type: PromptType): PromptTemplate? {
        val prompt = promptJpaRepository.findFirstByTypeAndDeletedAtIsNull(type) ?: return null
        val config = configJpaRepository.findByIdOrNull(prompt.aiModelConfigId) ?: return null
        val model = modelJpaRepository.findByIdOrNull(config.aiModelId) ?: return null

        return PromptTemplate(
            modelName = model.name,
            parameters = config.parameters,
            systemPrompt = prompt.content,
            jsonSchema = prompt.jsonSchema,
        )
    }
}
