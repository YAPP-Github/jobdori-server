package com.jobdori.api.application.profile.service

import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.profile.CoreCompetencyGeneration
import com.jobdori.core.application.profile.ProfileAiService
import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.ProfileSections
import com.jobdori.core.domain.profile.service.ProfileModifier
import com.jobdori.core.domain.profile.service.ProfileReader
import com.jobdori.core.domain.resume.service.ResumeModifier
import com.jobdori.core.domain.workspace.Workspace
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class ProfileServiceTest : StringSpec({

    val workspaceAccessValidationService = mockk<WorkspaceAccessValidationService>()
    val profileReader = mockk<ProfileReader>()
    val profileModifier = mockk<ProfileModifier>()
    val profileAiService = mockk<ProfileAiService>()
    val resumeModifier = mockk<ResumeModifier>()
    val profileService = ProfileService(
        workspaceAccessValidationService = workspaceAccessValidationService,
        profileReader = profileReader,
        profileModifier = profileModifier,
        profileAiService = profileAiService,
        resumeModifier = resumeModifier,
    )

    beforeTest {
        every {
            workspaceAccessValidationService.validateAccessible(
                workspaceId = "workspace-id",
                userId = 10L,
            )
        } returns Workspace(
            id = 1L,
            publicId = "workspace-id",
            ownerUserId = 10L,
        )
    }

    "핵심역량 생성 결과는 응답으로만 반환하고 이력서에는 생성 성공 여부만 기록한다" {
        val profile = Profile(1L, 1L, "홍길동", null, null, null)
        val profileDetail = ProfileDetail(
            profile = profile,
            sections = ProfileSections(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
        )
        every {
            resumeModifier.claimCoreCompetencyGeneration(workspaceId = 1L, resumeId = 100L)
        } returns true
        every {
            resumeModifier.completeCoreCompetencyGeneration(workspaceId = 1L, resumeId = 100L)
        } returns Unit
        every { profileReader.getOrCreateProfile(1L) } returns profile
        every { profileReader.getDetail(profile) } returns profileDetail
        every {
            profileAiService.generateCoreCompetency(profileDetail, workspaceId = 1L, jdPublicId = "jd-public-id")
        } returns CoreCompetencyGeneration(
            strategy = "지원 전략",
            coreCompetency = "AI 생성 핵심역량",
        )

        val response = profileService.generateCoreCompetency(
            userId = 10L,
            workspaceId = "workspace-id",
            resumeId = 100L,
            jdId = "jd-public-id",
        )

        response.coreCompetency shouldBe "AI 생성 핵심역량"
        response.strategy shouldBe "지원 전략"
        verify(exactly = 1) {
            resumeModifier.claimCoreCompetencyGeneration(workspaceId = 1L, resumeId = 100L)
        }
        verify(exactly = 1) {
            resumeModifier.completeCoreCompetencyGeneration(workspaceId = 1L, resumeId = 100L)
        }
    }

    "이미 핵심역량을 생성한 이력서는 다시 생성하지 않는다" {
        every {
            resumeModifier.claimCoreCompetencyGeneration(workspaceId = 1L, resumeId = 100L)
        } returns false

        shouldThrow<InvalidArgumentsException> {
            profileService.generateCoreCompetency(
                userId = 10L,
                workspaceId = "workspace-id",
                resumeId = 100L,
                jdId = "jd-public-id",
            )
        }

        verify(exactly = 0) {
            profileAiService.generateCoreCompetency(any(), any(), any())
        }
    }

    "핵심역량 생성에 실패하면 생성 여부 기록을 되돌린다" {
        val profile = Profile(1L, 1L, "홍길동", null, null, null)
        val profileDetail = ProfileDetail(
            profile = profile,
            sections = ProfileSections(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
        )
        every {
            resumeModifier.claimCoreCompetencyGeneration(workspaceId = 1L, resumeId = 100L)
        } returns true
        every { profileReader.getOrCreateProfile(1L) } returns profile
        every { profileReader.getDetail(profile) } returns profileDetail
        every {
            profileAiService.generateCoreCompetency(profileDetail, workspaceId = 1L, jdPublicId = "jd-public-id")
        } throws IllegalStateException("AI 생성 실패")
        every {
            resumeModifier.resetCoreCompetencyGeneration(workspaceId = 1L, resumeId = 100L)
        } returns Unit

        shouldThrow<IllegalStateException> {
            profileService.generateCoreCompetency(
                userId = 10L,
                workspaceId = "workspace-id",
                resumeId = 100L,
                jdId = "jd-public-id",
            )
        }

        verify(exactly = 1) {
            resumeModifier.resetCoreCompetencyGeneration(workspaceId = 1L, resumeId = 100L)
        }
        verify(exactly = 0) {
            resumeModifier.completeCoreCompetencyGeneration(any(), any())
        }
    }

})
