package com.jobdori.core.domain.resume.service

import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.ResumeDetailSection
import com.jobdori.core.domain.resume.ResumeFixture
import com.jobdori.core.domain.resume.ResumeSectionFixture
import com.jobdori.core.domain.resume.ResumeSectionItemFixture
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.resume.error.ResumeNotFoundException
import com.jobdori.core.domain.resume.repository.ResumeRepository
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionItemSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionSaveCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ResumeModifierTest : StringSpec({

    val resumeRepository = mockk<ResumeRepository>()
    val resumeModifier = ResumeModifier(
        resumeRepository = resumeRepository,
    )

    "이력서 상세를 수정한다" {
        // given
        val command = saveCommand(title = "수정 이력서")
        val detail = ResumeDetail(
            resume = ResumeFixture.create(id = 1L, workspaceId = 10L, title = "수정 이력서"),
            sections = listOf(
                ResumeDetailSection(
                    section = ResumeSectionFixture.create(id = 2L, resumeId = 1L),
                    items = listOf(ResumeSectionItemFixture.create(id = 3L, sectionId = 2L)),
                ),
            ),
        )
        every {
            resumeRepository.modifyDetail(
                id = 1L,
                workspaceId = 10L,
                command = command,
            )
        } returns detail

        // when & then
        resumeModifier.modifyDetail(
            workspaceId = 10L,
            resumeId = 1L,
            command = command,
        ) shouldBe detail
    }

    "수정할 이력서가 없으면 예외를 던진다" {
        // given
        val command = saveCommand(title = "수정 이력서")
        every {
            resumeRepository.modifyDetail(
                id = 1L,
                workspaceId = 10L,
                command = command,
            )
        } returns null

        // when & then
        shouldThrow<ResumeNotFoundException> {
            resumeModifier.modifyDetail(
                workspaceId = 10L,
                resumeId = 1L,
                command = command,
            )
        }
    }

})

private fun saveCommand(title: String) = ResumeSaveCommand(
    targetJdId = null,
    title = title,
    template = ResumeTemplate.DEFAULT,
    status = ResumeStatus.DRAFT,
    sections = listOf(
        ResumeSectionSaveCommand(
            sectionId = 2L,
            type = ResumeSectionType.BASIC_INFO,
            displayOrder = 10.0,
            visible = true,
            items = listOf(
                ResumeSectionItemSaveCommand(
                    itemId = 3L,
                    payload = ResumeBasicInfoPayload(
                        name = "홍길동",
                        email = "hong@example.com",
                        phone = "010-0000-0000",
                    ),
                    displayOrder = 1.0,
                    visible = true,
                ),
            ),
        ),
    ),
)
