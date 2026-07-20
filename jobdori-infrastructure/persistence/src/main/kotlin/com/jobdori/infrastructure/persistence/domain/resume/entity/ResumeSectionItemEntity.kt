package com.jobdori.infrastructure.persistence.domain.resume.entity

import com.jobdori.common.json.JsonUtils
import com.jobdori.core.domain.resume.ResumeAwardPayload
import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeCareerPayload
import com.jobdori.core.domain.resume.ResumeCertificatePayload
import com.jobdori.core.domain.resume.ResumeCoreSkillPayload
import com.jobdori.core.domain.resume.ResumeEducationPayload
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeLanguagePayload
import com.jobdori.core.domain.resume.ResumeSectionItem
import com.jobdori.core.domain.resume.ResumeSectionItemPayload
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeSkillPayload
import com.jobdori.core.support.crypto.StringEncryptor
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Table(name = "resume_section_item_v1")
@Entity
class ResumeSectionItemEntity(
    @Column(nullable = false)
    var sectionId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var payloadType: ResumeSectionType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var payload: String,

    @Column(nullable = false)
    var displayOrder: Double,

    @Column(nullable = false)
    var visible: Boolean,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain(encryptor: StringEncryptor) = ResumeSectionItem(
        id = id,
        sectionId = sectionId,
        payload = parsePayload(payloadType, payload, encryptor),
        displayOrder = displayOrder,
        visible = visible,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun from(domain: ResumeSectionItem, encryptor: StringEncryptor) = ResumeSectionItemEntity(
            sectionId = domain.sectionId,
            payloadType = domain.payload.type,
            payload = serializePayload(domain.payload, encryptor),
            displayOrder = domain.displayOrder,
            visible = domain.visible,
        ).also { it.id = domain.id }

        private fun parsePayload(
            payloadType: ResumeSectionType,
            payload: String,
            encryptor: StringEncryptor,
        ): ResumeSectionItemPayload {
            return when (payloadType) {
                ResumeSectionType.BASIC_INFO ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeBasicInfoPayload::class.java)).decrypt(encryptor)

                ResumeSectionType.CORE_SKILL ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeCoreSkillPayload::class.java))

                ResumeSectionType.CAREER ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeCareerPayload::class.java))

                ResumeSectionType.EXPERIENCE ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeExperiencePayload::class.java))

                ResumeSectionType.EDUCATION ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeEducationPayload::class.java))

                ResumeSectionType.AWARD ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeAwardPayload::class.java))

                ResumeSectionType.CERTIFICATE ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeCertificatePayload::class.java))

                ResumeSectionType.LANGUAGE ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeLanguagePayload::class.java))

                ResumeSectionType.SKILL ->
                    requireNotNull(JsonUtils.toObject(payload, ResumeSkillPayload::class.java))
            }
        }

        fun serializePayload(payload: ResumeSectionItemPayload, encryptor: StringEncryptor): String {
            val persistencePayload = if (payload is ResumeBasicInfoPayload) payload.encrypt(encryptor) else payload
            return JsonUtils.toJson(persistencePayload)
        }

        private fun ResumeBasicInfoPayload.encrypt(encryptor: StringEncryptor) = copy(
            name = name?.let(encryptor::encrypt),
            email = email?.let(encryptor::encrypt),
            phone = phone?.let(encryptor::encrypt),
        )

        private fun ResumeBasicInfoPayload.decrypt(encryptor: StringEncryptor) = copy(
            name = name?.let(encryptor::decrypt),
            email = email?.let(encryptor::decrypt),
            phone = phone?.let(encryptor::decrypt),
        )
    }

}
