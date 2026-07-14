package com.jobdori.infrastructure.persistence.domain.profile.repository

import com.jobdori.core.domain.profile.ProfileSections
import com.jobdori.core.domain.profile.repository.ProfileSectionRepository
import com.jobdori.core.domain.profile.section.Award
import com.jobdori.core.domain.profile.section.Career
import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.core.domain.profile.section.ProfileSkill
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileAwardEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileCareerEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileCertificationEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileEducationEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileLanguageTestEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileSkillEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProfileSectionRepositoryImpl(
    private val educationJpaRepository: ProfileEducationJpaRepository,
    private val careerJpaRepository: ProfileCareerJpaRepository,
    private val languageTestJpaRepository: ProfileLanguageTestJpaRepository,
    private val awardJpaRepository: ProfileAwardJpaRepository,
    private val certificationJpaRepository: ProfileCertificationJpaRepository,
    private val skillJpaRepository: ProfileSkillJpaRepository,
) : ProfileSectionRepository {

    @Transactional(readOnly = true)
    override fun findAllByProfileId(profileId: Long): ProfileSections {
        return ProfileSections(
            educations = educationJpaRepository.findAllByProfileIdOrderByDisplayOrder(profileId).map { it.toDomain() },
            careers = careerJpaRepository.findAllByProfileIdOrderByDisplayOrder(profileId).map { it.toDomain() },
            languageTests = languageTestJpaRepository.findAllByProfileIdOrderByDisplayOrder(profileId)
                .map { it.toDomain() },
            awards = awardJpaRepository.findAllByProfileIdOrderByDisplayOrder(profileId).map { it.toDomain() },
            certifications = certificationJpaRepository.findAllByProfileIdOrderByDisplayOrder(profileId)
                .map { it.toDomain() },
            skills = skillJpaRepository.findAllByProfileIdOrderByDisplayOrder(profileId).map { it.toDomain() },
        )
    }

    @Transactional
    override fun replaceEducations(profileId: Long, items: List<Education>): List<Education> {
        educationJpaRepository.deleteAllByProfileId(profileId)
        return educationJpaRepository
            .saveAll(items.mapIndexed { index, item -> ProfileEducationEntity.from(profileId, index, item) })
            .map { it.toDomain() }
    }

    @Transactional
    override fun replaceCareers(profileId: Long, items: List<Career>): List<Career> {
        careerJpaRepository.deleteAllByProfileId(profileId)
        return careerJpaRepository
            .saveAll(items.mapIndexed { index, item -> ProfileCareerEntity.from(profileId, index, item) })
            .map { it.toDomain() }
    }

    @Transactional
    override fun replaceLanguageTests(profileId: Long, items: List<LanguageTest>): List<LanguageTest> {
        languageTestJpaRepository.deleteAllByProfileId(profileId)
        return languageTestJpaRepository
            .saveAll(items.mapIndexed { index, item -> ProfileLanguageTestEntity.from(profileId, index, item) })
            .map { it.toDomain() }
    }

    @Transactional
    override fun replaceAwards(profileId: Long, items: List<Award>): List<Award> {
        awardJpaRepository.deleteAllByProfileId(profileId)
        return awardJpaRepository
            .saveAll(items.mapIndexed { index, item -> ProfileAwardEntity.from(profileId, index, item) })
            .map { it.toDomain() }
    }

    @Transactional
    override fun replaceCertifications(profileId: Long, items: List<Certification>): List<Certification> {
        certificationJpaRepository.deleteAllByProfileId(profileId)
        return certificationJpaRepository
            .saveAll(items.mapIndexed { index, item -> ProfileCertificationEntity.from(profileId, index, item) })
            .map { it.toDomain() }
    }

    @Transactional
    override fun replaceSkills(profileId: Long, items: List<ProfileSkill>): List<ProfileSkill> {
        skillJpaRepository.deleteAllByProfileId(profileId)
        return skillJpaRepository
            .saveAll(items.mapIndexed { index, item -> ProfileSkillEntity.from(profileId, index, item) })
            .map { it.toDomain() }
    }

}
