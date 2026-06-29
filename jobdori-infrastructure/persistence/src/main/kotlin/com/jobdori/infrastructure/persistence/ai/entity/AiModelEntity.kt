package com.jobdori.infrastructure.persistence.ai.entity

import com.jobdori.core.domain.ai.AiVendor
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table


@Entity
@Table(name = "ai_models_v1")
class AiModelEntity(
    @Column(length = 100, nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    var vendor: AiVendor,
) : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}
