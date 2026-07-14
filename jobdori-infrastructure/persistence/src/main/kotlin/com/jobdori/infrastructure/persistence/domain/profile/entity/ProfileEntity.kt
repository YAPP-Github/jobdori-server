package com.jobdori.infrastructure.persistence.domain.profile.entity

import com.jobdori.core.domain.profile.Profile
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

    @Column(length = 50)
    var name: String?,

    @Column(length = 30)
    var phone: String?,

    @Column(length = 100)
    var email: String?,

    @Column(columnDefinition = "text")
    var coreCompetency: String?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = Profile(
        id = id,
        workspaceId = workspaceId,
        name = name,
        phone = phone,
        email = email,
        coreCompetency = coreCompetency,
    )

    companion object {
        fun from(domain: Profile) = ProfileEntity(
            workspaceId = domain.workspaceId,
            name = domain.name,
            phone = domain.phone,
            email = domain.email,
            coreCompetency = domain.coreCompetency,
        ).also { it.id = domain.id }
    }

}
