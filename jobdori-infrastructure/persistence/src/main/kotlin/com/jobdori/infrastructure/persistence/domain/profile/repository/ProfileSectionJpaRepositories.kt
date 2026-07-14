package com.jobdori.infrastructure.persistence.domain.profile.repository

import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileAwardEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileCareerEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileCertificationEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileEducationEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileLanguageTestEntity
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileSkillEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProfileEducationJpaRepository : JpaRepository<ProfileEducationEntity, Long> {
    fun findAllByProfileIdOrderByDisplayOrder(profileId: Long): List<ProfileEducationEntity>
    fun deleteAllByProfileId(profileId: Long)
}

interface ProfileCareerJpaRepository : JpaRepository<ProfileCareerEntity, Long> {
    fun findAllByProfileIdOrderByDisplayOrder(profileId: Long): List<ProfileCareerEntity>
    fun deleteAllByProfileId(profileId: Long)
}

interface ProfileLanguageTestJpaRepository : JpaRepository<ProfileLanguageTestEntity, Long> {
    fun findAllByProfileIdOrderByDisplayOrder(profileId: Long): List<ProfileLanguageTestEntity>
    fun deleteAllByProfileId(profileId: Long)
}

interface ProfileAwardJpaRepository : JpaRepository<ProfileAwardEntity, Long> {
    fun findAllByProfileIdOrderByDisplayOrder(profileId: Long): List<ProfileAwardEntity>
    fun deleteAllByProfileId(profileId: Long)
}

interface ProfileCertificationJpaRepository : JpaRepository<ProfileCertificationEntity, Long> {
    fun findAllByProfileIdOrderByDisplayOrder(profileId: Long): List<ProfileCertificationEntity>
    fun deleteAllByProfileId(profileId: Long)
}

interface ProfileSkillJpaRepository : JpaRepository<ProfileSkillEntity, Long> {
    fun findAllByProfileIdOrderByDisplayOrder(profileId: Long): List<ProfileSkillEntity>
    fun deleteAllByProfileId(profileId: Long)
}
