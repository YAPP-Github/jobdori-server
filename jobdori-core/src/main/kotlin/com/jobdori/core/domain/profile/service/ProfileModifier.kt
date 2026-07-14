package com.jobdori.core.domain.profile.service

import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.ProfileSections
import com.jobdori.core.domain.profile.repository.ProfileRepository
import com.jobdori.core.domain.profile.repository.ProfileSectionRepository
import com.jobdori.core.domain.profile.service.command.ProfileUpdateCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfileModifier(
    private val profileRepository: ProfileRepository,
    private val profileSectionRepository: ProfileSectionRepository,
) {

    // command의 null 필드는 미변경으로 유지, 섹션 리스트는 전체 교체(배열 순서 = 노출 순서)
    @Transactional
    fun modify(profile: Profile, command: ProfileUpdateCommand): ProfileDetail {
        val saved = profileRepository.save(
            profile.copy(
                name = command.name ?: profile.name,
                phone = command.phone ?: profile.phone,
                email = command.email ?: profile.email,
                coreCompetency = command.coreCompetency ?: profile.coreCompetency,
            ),
        )

        val current = profileSectionRepository.findAllByProfileId(profile.id)
        val sections = ProfileSections(
            educations = command.educations
                ?.let { profileSectionRepository.replaceEducations(profile.id, it) }
                ?: current.educations,
            careers = command.careers
                ?.let { profileSectionRepository.replaceCareers(profile.id, it) }
                ?: current.careers,
            languageTests = command.languageTests
                ?.let { profileSectionRepository.replaceLanguageTests(profile.id, it) }
                ?: current.languageTests,
            awards = command.awards
                ?.let { profileSectionRepository.replaceAwards(profile.id, it) }
                ?: current.awards,
            certifications = command.certifications
                ?.let { profileSectionRepository.replaceCertifications(profile.id, it) }
                ?: current.certifications,
            skills = command.skills
                ?.let { profileSectionRepository.replaceSkills(profile.id, it) }
                ?: current.skills,
        )

        return ProfileDetail(profile = saved, sections = sections)
    }

}
