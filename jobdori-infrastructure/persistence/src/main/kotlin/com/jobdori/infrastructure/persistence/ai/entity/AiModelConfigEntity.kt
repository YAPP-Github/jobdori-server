package com.jobdori.infrastructure.persistence.ai.entity

import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "ai_model_configs_v1")
class AiModelConfigEntity(
    @Column(nullable = false)
    var aiModelId: Long,

    @Column(length = 100)
    var name: String,

    @Column(columnDefinition = "text")
    var description: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    var parameters: AiParameters,

): AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

}
