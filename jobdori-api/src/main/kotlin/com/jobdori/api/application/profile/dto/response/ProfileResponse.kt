package com.jobdori.api.application.profile.dto.response

import com.jobdori.core.domain.profile.ProfileDetail

data class ProfileResponse(
    val profileId: Long,
    val name: String?,
    val phone: String?,
    val email: String?,
    val coreCompetency: String?,
    val educations: List<ProfileEducationResponse>,
    val careers: List<ProfileCareerResponse>,
    val languageTests: List<ProfileLanguageTestResponse>,
    val awards: List<ProfileAwardResponse>,
    val certifications: List<ProfileCertificationResponse>,
    val skills: List<ProfileSkillResponse>,
) {

    companion object {
        fun from(detail: ProfileDetail) = ProfileResponse(
            profileId = detail.profile.id,
            name = detail.profile.name,
            phone = detail.profile.phone,
            email = detail.profile.email,
            coreCompetency = detail.profile.coreCompetency,
            educations = detail.sections.educations.map { ProfileEducationResponse.from(it) },
            careers = detail.sections.careers.map { ProfileCareerResponse.from(it) },
            languageTests = detail.sections.languageTests.map { ProfileLanguageTestResponse.from(it) },
            awards = detail.sections.awards.map { ProfileAwardResponse.from(it) },
            certifications = detail.sections.certifications.map { ProfileCertificationResponse.from(it) },
            skills = detail.sections.skills.map { ProfileSkillResponse.from(it) },
        )
    }

}
