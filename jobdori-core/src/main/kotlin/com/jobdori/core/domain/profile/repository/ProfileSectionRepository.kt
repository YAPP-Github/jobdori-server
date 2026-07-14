package com.jobdori.core.domain.profile.repository

import com.jobdori.core.domain.profile.ProfileSections
import com.jobdori.core.domain.profile.section.Award
import com.jobdori.core.domain.profile.section.Career
import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.core.domain.profile.section.ProfileSkill

// 섹션은 Profile 애그리게이트의 하위 요소라 개별 리포지토리로 나누지 않고 하나로 묶는다.
// 저장은 전체 교체(replace) semantics: 배열 순서가 display_order의 진실이다.
interface ProfileSectionRepository {

    fun findAllByProfileId(profileId: Long): ProfileSections

    fun replaceEducations(profileId: Long, items: List<Education>): List<Education>

    fun replaceCareers(profileId: Long, items: List<Career>): List<Career>

    fun replaceLanguageTests(profileId: Long, items: List<LanguageTest>): List<LanguageTest>

    fun replaceAwards(profileId: Long, items: List<Award>): List<Award>

    fun replaceCertifications(profileId: Long, items: List<Certification>): List<Certification>

    fun replaceSkills(profileId: Long, items: List<ProfileSkill>): List<ProfileSkill>

}
