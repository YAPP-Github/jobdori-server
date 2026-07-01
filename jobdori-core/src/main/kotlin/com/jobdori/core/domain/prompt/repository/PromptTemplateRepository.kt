package com.jobdori.core.domain.prompt.repository

import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType

interface PromptTemplateRepository {
    /** 타입의 SYSTEM 프롬프트 + 모델/파라미터/jsonSchema를 조립해 반환. 없으면 null. */
    fun findByType(type: PromptType): PromptTemplate?
}
