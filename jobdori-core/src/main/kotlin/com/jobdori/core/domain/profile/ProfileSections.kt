package com.jobdori.core.domain.profile

import com.jobdori.core.domain.profile.section.Award
import com.jobdori.core.domain.profile.section.Career
import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.core.domain.profile.section.ProfileSkill

data class ProfileSections(
    val educations: List<Education>,
    val careers: List<Career>,
    val languageTests: List<LanguageTest>,
    val awards: List<Award>,
    val certifications: List<Certification>,
    val skills: List<ProfileSkill>,
)
