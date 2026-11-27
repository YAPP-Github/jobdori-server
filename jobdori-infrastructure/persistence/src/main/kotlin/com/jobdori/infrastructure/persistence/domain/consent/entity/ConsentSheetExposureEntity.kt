package com.jobdori.infrastructure.persistence.domain.consent.entity

import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "consent_sheet_exposure_v1")
@Entity
class ConsentSheetExposureEntity(
    @Id
    @Column(name = "user_id")
    var userId: Long,
) : AuditableEntity()
