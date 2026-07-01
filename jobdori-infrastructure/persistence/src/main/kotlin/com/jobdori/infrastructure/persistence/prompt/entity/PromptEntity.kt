package com.jobdori.infrastructure.persistence.prompt.entity

import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "prompts_v1")
class PromptEntity(
    @Column(nullable = false)
    var aiModelConfigId: Long,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "text")
    var type: PromptType,

    @Column(columnDefinition = "text")
    var content: String,   // SYSTEM 프롬프트(B안 — type당 1행, prompt_role 없음)

    @Column(columnDefinition = "text")
    var jsonSchema: String?,  // 제공 스키마에 없던 우리 추가 컬럼(구조화 추출용)

    var deletedAt: LocalDateTime? = null,

    ): AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}
