package com.jobdori.core.application.experience

import com.jobdori.core.application.experience.command.ImportedExperienceCommandGroup
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceProjectFixture
import com.jobdori.core.domain.experience.service.ExperienceCreator
import com.jobdori.core.domain.experience.service.ExperienceProjectCreator
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class ExperienceImportServiceTest : StringSpec({

    val experienceProjectCreator = mockk<ExperienceProjectCreator>()
    val experienceCreator = mockk<ExperienceCreator>()
    val experienceImportService = ExperienceImportService(
        experienceProjectCreator = experienceProjectCreator,
        experienceCreator = experienceCreator,
    )

    "가져온 경험 그룹을 프로젝트 생성 후 경험 생성으로 저장한다" {
        // given
        val firstProjectCommand = ExperienceProjectCreateCommand(
            name = "채용 플랫폼 지원 자동화",
            summary = "공고 탐색부터 지원 현황 관리까지 개선",
            period = null,
            role = "백엔드 개발",
        )
        val secondProjectCommand = ExperienceProjectCreateCommand(
            name = "이력서 PDF 경험 추출",
            summary = "PDF에서 경험 후보를 구조화",
            period = null,
            role = "서비스 개발",
        )
        val firstExperiences = listOf(
            ExperienceCreateCommand(
                tags = listOf("Kotlin", "Spring"),
                title = "지원 현황 관리 API 설계",
                contents = ExperienceContents.free("지원 현황 관리 API를 설계했다"),
            ),
            ExperienceCreateCommand(
                tags = listOf("REST Docs"),
                title = "문서화 테스트 기반 API 검증",
                contents = ExperienceContents.free("문서화 테스트를 작성했다"),
            ),
        )
        val secondExperiences = listOf(
            ExperienceCreateCommand(
                tags = listOf("PDF", "Import"),
                title = "PDF 텍스트 추출 실패 처리",
                contents = ExperienceContents.free("PDF 파싱 실패를 사용자 오류로 변환했다"),
            ),
        )
        val groups = listOf(
            ImportedExperienceCommandGroup(
                project = firstProjectCommand,
                experiences = firstExperiences,
            ),
            ImportedExperienceCommandGroup(
                project = secondProjectCommand,
                experiences = secondExperiences,
            ),
        )

        every {
            experienceProjectCreator.create(
                workspaceId = 1L,
                commands = listOf(firstProjectCommand, secondProjectCommand),
            )
        } returns listOf(
            ExperienceProjectFixture.create(id = 10L),
            ExperienceProjectFixture.create(id = 20L),
        )
        every {
            experienceCreator.create(workspaceId = 1L, projectId = 10L, commands = firstExperiences)
        } returns emptyList()
        every {
            experienceCreator.create(workspaceId = 1L, projectId = 20L, commands = secondExperiences)
        } returns emptyList()

        // when
        experienceImportService.saveAll(
            workspaceId = 1L,
            groups = groups,
        )

        // then
        verify(exactly = 1) {
            experienceProjectCreator.create(
                workspaceId = 1L,
                commands = listOf(firstProjectCommand, secondProjectCommand),
            )
        }
        verify(exactly = 1) {
            experienceCreator.create(workspaceId = 1L, projectId = 10L, commands = firstExperiences)
        }
        verify(exactly = 1) {
            experienceCreator.create(workspaceId = 1L, projectId = 20L, commands = secondExperiences)
        }
    }

    "가져온 경험 그룹이 비어 있으면 저장하지 않는다" {
        // when
        experienceImportService.saveAll(
            workspaceId = 1L,
            groups = emptyList(),
        )

        // then
        verify(exactly = 0) { experienceProjectCreator.create(workspaceId = any(), commands = any()) }
        verify(exactly = 0) { experienceCreator.create(workspaceId = any(), projectId = any(), commands = any()) }
    }

})
