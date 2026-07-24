package com.jobdori.core.domain.resume.service

import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.ProfileSections
import com.jobdori.core.domain.profile.section.Award
import com.jobdori.core.domain.profile.section.Career
import com.jobdori.core.domain.profile.section.Certification
import com.jobdori.core.domain.profile.section.Degree
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.EducationStatus
import com.jobdori.core.domain.profile.section.LanguageTest
import com.jobdori.core.domain.profile.section.ProfileSkill
import com.jobdori.core.domain.profile.section.SkillLevel
import com.jobdori.core.domain.resume.ResumeCoreSkillPayload
import com.jobdori.core.domain.resume.ResumeSectionType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class ProfileResumeSectionInitializerTest : StringSpec({

    val initializer = ProfileResumeSectionInitializer()

    "프로필의 기본 항목을 경험을 제외한 이력서 섹션으로 초기화한다" {
        val detail = ProfileDetail(
            profile = Profile(
                id = 1L,
                workspaceId = 2L,
                name = "홍길동",
                phone = "010-0000-0000",
                email = "hong@example.com",
                coreCompetency = "문제 해결 역량",
            ),
            sections = ProfileSections(
                educations = listOf(Education("잡도리대학교", "컴퓨터공학", Degree.BACHELOR, EducationStatus.GRADUATED, null)),
                careers = listOf(Career("잡도리", "백엔드 개발자", null, "API 개발")),
                languageTests = listOf(LanguageTest("TOEIC", "900", LocalDate.of(2026, 1, 1))),
                awards = listOf(Award("우수상", "잡도리", LocalDate.of(2025, 1, 1))),
                certifications = listOf(Certification("정보처리기사", "한국산업인력공단", LocalDate.of(2024, 1, 1))),
                skills = listOf(ProfileSkill("Kotlin", SkillLevel.HIGH)),
            ),
        )

        val initializedTypes = listOf(
            ResumeSectionType.BASIC_INFO,
            ResumeSectionType.CORE_SKILL,
            ResumeSectionType.CAREER,
            ResumeSectionType.EDUCATION,
            ResumeSectionType.AWARD,
            ResumeSectionType.LANGUAGE,
            ResumeSectionType.CERTIFICATE,
            ResumeSectionType.SKILL,
        )

        initializedTypes.map { type -> initializer.initializeItems(detail, type).single().payload.type } shouldBe listOf(
            ResumeSectionType.BASIC_INFO,
            ResumeSectionType.CORE_SKILL,
            ResumeSectionType.CAREER,
            ResumeSectionType.EDUCATION,
            ResumeSectionType.AWARD,
            ResumeSectionType.LANGUAGE,
            ResumeSectionType.CERTIFICATE,
            ResumeSectionType.SKILL,
        )
        initializedTypes.flatMap { initializer.initializeItems(detail, it) }
            .all { it.visible && it.itemId == null } shouldBe true
        initializer.initializeItems(detail, ResumeSectionType.CORE_SKILL)
            .single().payload shouldBe ResumeCoreSkillPayload(
            content = "문제 해결 역량",
            isInitialItem = true,
        )
    }

    "필수값이 비어 있는 프로필 항목은 섹션으로 만들지 않는다" {
        val detail = ProfileDetail(
            profile = Profile(1L, 2L, " ", null, null, ""),
            sections = ProfileSections(
                educations = listOf(Education(null, null, null, null, null)),
                careers = listOf(Career("", null, null, null)),
                languageTests = listOf(LanguageTest("TOEIC", null, null)),
                awards = listOf(Award(null, null, null)),
                certifications = listOf(Certification(null, null, null)),
                skills = listOf(ProfileSkill(" ", null)),
            ),
        )

        ResumeSectionType.entries.flatMap { initializer.initializeItems(detail, it) } shouldBe emptyList()
    }
})
