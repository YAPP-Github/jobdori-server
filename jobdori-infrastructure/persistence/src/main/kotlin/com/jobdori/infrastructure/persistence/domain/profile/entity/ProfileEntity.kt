package com.jobdori.infrastructure.persistence.domain.profile.entity

import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.support.crypto.StringEncryptor
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate

@Table(name = "profile_v1")
@Entity
@DynamicUpdate
class ProfileEntity(
    @Column(nullable = false, unique = true)
    var workspaceId: Long,

    @Column(name = "name_encrypted", columnDefinition = "text")
    var nameEncrypted: String?,

    @Column(name = "phone_encrypted", columnDefinition = "text")
    var phoneEncrypted: String?,

    @Column(name = "email_encrypted", columnDefinition = "text")
    var emailEncrypted: String?,

    @Column(columnDefinition = "text")
    var coreCompetency: String?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain(encryptor: StringEncryptor) = Profile(
        id = id,
        workspaceId = workspaceId,
        name = nameEncrypted?.let(encryptor::decrypt),
        phone = phoneEncrypted?.let(encryptor::decrypt),
        email = emailEncrypted?.let(encryptor::decrypt),
        coreCompetency = coreCompetency,
    )

    companion object {
        fun from(domain: Profile, encryptor: StringEncryptor) = ProfileEntity(
            workspaceId = domain.workspaceId,
            nameEncrypted = domain.name?.let(encryptor::encrypt),
            phoneEncrypted = domain.phone?.let(encryptor::encrypt),
            emailEncrypted = domain.email?.let(encryptor::encrypt),
            coreCompetency = domain.coreCompetency,
        ).also { it.id = domain.id }
    }

}
