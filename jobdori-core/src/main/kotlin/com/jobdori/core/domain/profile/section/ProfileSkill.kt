package com.jobdori.core.domain.profile.section

data class ProfileSkill(
    val name: String?,
    val level: SkillLevel?,
)

enum class SkillLevel {
    HIGH,
    MEDIUM,
    LOW,
}
