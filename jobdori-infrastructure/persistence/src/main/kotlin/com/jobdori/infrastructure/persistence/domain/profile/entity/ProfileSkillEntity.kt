package com.jobdori.infrastructure.persistence.domain.profile.entity

import com.jobdori.core.domain.profile.section.ProfileSkill
import com.jobdori.core.domain.profile.section.SkillLevel
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "profile_skill_v1")
@Entity
class ProfileSkillEntity(
    @Column(nullable = false)
    var profileId: Long,

    @Column(nullable = false)
    var displayOrder: Int,

    @Column(length = 100)
    var name: String?,

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    var level: SkillLevel?,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

    fun toDomain() = ProfileSkill(
        name = name,
        level = level,
    )

    companion object {
        fun from(profileId: Long, displayOrder: Int, domain: ProfileSkill) = ProfileSkillEntity(
            profileId = profileId,
            displayOrder = displayOrder,
            name = domain.name,
            level = domain.level,
        )
    }

}
