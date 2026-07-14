package com.jobdori.core.domain.profile.service.command

import com.jobdori.core.domain.profile.section.Award
import com.jobdori.core.domain.profile.section.Career
import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.core.domain.profile.section.ProfileSkill

// null = 미변경, 빈 리스트 = 비우기 (partial update)
data class ProfileUpdateCommand(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val coreCompetency: String? = null,
    val educations: List<Education>? = null,
    val careers: List<Career>? = null,
    val languageTests: List<LanguageTest>? = null,
    val awards: List<Award>? = null,
    val certifications: List<Certification>? = null,
    val skills: List<ProfileSkill>? = null,
)
