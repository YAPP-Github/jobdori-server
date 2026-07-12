package com.jobdori.api.application.resume.service

import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.api.application.resume.dto.request.ResumeBasicInfoPayloadRequest
import com.jobdori.api.application.resume.dto.request.ResumeLanguagePayloadRequest
import com.jobdori.api.application.resume.dto.request.ResumeSectionItemPayloadRequest
import com.jobdori.api.application.resume.dto.request.SaveResumeRequest
import com.jobdori.api.application.resume.dto.request.SaveResumeSectionItemRequest
import com.jobdori.api.application.resume.dto.request.SaveResumeSectionRequest
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.ResumeDetailSection
import com.jobdori.core.domain.resume.ResumeFixture
import com.jobdori.core.domain.resume.ResumeLanguagePayload
import com.jobdori.core.domain.resume.ResumeSectionFixture
import com.jobdori.core.domain.resume.ResumeSectionItemFixture
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.resume.service.ResumeCreator
import com.jobdori.core.domain.resume.service.ResumeModifier
import com.jobdori.core.domain.resume.service.ResumeReader
import com.jobdori.core.domain.resume.service.ResumeRemover
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.domain.workspace.Workspace
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate

class ResumeServiceTest : StringSpec({

    val workspaceAccessValidationService = mockk<WorkspaceAccessValidationService>()
    val resumeCreator = mockk<ResumeCreator>()
    val resumeReader = mockk<ResumeReader>()
    val resumeRemover = mockk<ResumeRemover>()
    val resumeModifier = mockk<ResumeModifier>()
    val resumeService = ResumeService(
        workspaceAccessValidationService = workspaceAccessValidationService,
        resumeCreator = resumeCreator,
        resumeReader = resumeReader,
        resumeRemover = resumeRemover,
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

    "이력서 수정 요청을 command로 변환해 modifier에 위임한다" {
        // given
        val request = saveRequest()
        val detail = ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L),
            sections = listOf(
                ResumeDetailSection(
                    section = ResumeSectionFixture.create(id = 200L, resumeId = 100L),
                    items = listOf(
                        ResumeSectionItemFixture.create(
                            id = 300L,
                            sectionId = 200L,
                            payload = ResumeBasicInfoPayload(
                                name = "홍길동",
                                email = "hong@example.com",
                                phone = "010-0000-0000",
                            ),
                        ),
                    ),
                ),
            ),
        )
        every {
            resumeModifier.modifyDetail(
                workspaceId = 1L,
                resumeId = 100L,
                command = any(),
            )
        } returns detail

        // when
        val response = resumeService.modifyResume(
            userId = 10L,
            workspaceId = "workspace-id",
            resumeId = 100L,
            request = request,
        )

        // then
        response.resumeId shouldBe 100L
        response.sections.single().items.single().payload.basicInfo?.name shouldBe "홍길동"
        verify(exactly = 1) {
            resumeModifier.modifyDetail(
                workspaceId = 1L,
                resumeId = 100L,
                command = withArg<ResumeSaveCommand> {
                    it.sections.single().items.single().payload.type shouldBe ResumeSectionType.BASIC_INFO
                },
            )
        }
    }

    "요청 섹션 타입으로 어학 섹션 command를 생성한다" {
        // given
        val request = languageSaveRequest()
        val detail = ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L),
            sections = listOf(
                ResumeDetailSection(
                    section = ResumeSectionFixture.create(
                        id = 200L,
                        resumeId = 100L,
                        type = ResumeSectionType.LANGUAGE,
                    ),
                    items = listOf(
                        ResumeSectionItemFixture.create(
                            id = 300L,
                            sectionId = 200L,
                            payload = ResumeLanguagePayload(
                                examName = "TOEIC",
                                scoreOrGrade = "900",
                                acquiredAt = LocalDate.of(2026, 1, 1),
                            ),
                        ),
                    ),
                ),
            ),
        )
        every {
            resumeModifier.modifyDetail(
                workspaceId = 1L,
                resumeId = 100L,
                command = any(),
            )
        } returns detail

        // when
        val response = resumeService.modifyResume(
            userId = 10L,
            workspaceId = "workspace-id",
            resumeId = 100L,
            request = request,
        )

        // then
        response.sections.single().type shouldBe ResumeSectionType.LANGUAGE
        response.sections.single().items.single().payload.language?.scoreOrGrade shouldBe "900"
        verify(exactly = 1) {
            resumeModifier.modifyDetail(
                workspaceId = 1L,
                resumeId = 100L,
                command = withArg<ResumeSaveCommand> {
                    it.sections.single().type shouldBe ResumeSectionType.LANGUAGE
                    it.sections.single().items.single().payload.type shouldBe ResumeSectionType.LANGUAGE
                },
            )
        }
    }

    "이력서 조회 응답의 섹션과 아이템을 displayOrder 오름차순으로 정렬한다" {
        // given
        every {
            resumeReader.getDetail(
                workspaceId = 1L,
                resumeId = 100L,
            )
        } returns ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L),
            sections = listOf(
                ResumeDetailSection(
                    section = ResumeSectionFixture.create(
                        id = 300L,
                        resumeId = 100L,
                        displayOrder = 20.0,
                    ),
                    items = listOf(
                        ResumeSectionItemFixture.create(
                            id = 302L,
                            sectionId = 300L,
                            displayOrder = 2.0,
                        ),
                        ResumeSectionItemFixture.create(
                            id = 301L,
                            sectionId = 300L,
                            displayOrder = 1.0,
                        ),
                    ),
                ),
                ResumeDetailSection(
                    section = ResumeSectionFixture.create(
                        id = 200L,
                        resumeId = 100L,
                        displayOrder = 10.0,
                    ),
                    items = listOf(
                        ResumeSectionItemFixture.create(
                            id = 202L,
                            sectionId = 200L,
                            displayOrder = 2.0,
                        ),
                        ResumeSectionItemFixture.create(
                            id = 201L,
                            sectionId = 200L,
                            displayOrder = 1.0,
                        ),
                    ),
                ),
            ),
        )

        // when
        val response = resumeService.getResume(
            userId = 10L,
            workspaceId = "workspace-id",
            resumeId = 100L,
            includeSections = true,
            includeSectionItems = true,
        )

        // then
        response.sections.map { it.sectionId } shouldBe listOf(200L, 300L)
        response.sections[0].items.map { it.itemId } shouldBe listOf(201L, 202L)
        response.sections[1].items.map { it.itemId } shouldBe listOf(301L, 302L)
    }

    "이력서 저장 요청의 섹션 displayOrder가 중복되면 예외가 발생한다" {
        // given
        val request = SaveResumeRequest(
            targetJdId = null,
            template = ResumeTemplate.DEFAULT,
            status = ResumeStatusType.DRAFT,
            sections = listOf(
                basicInfoSection(displayOrder = 1.0),
                languageSection(displayOrder = 1.0),
            ),
        )

        // when
        val exception = shouldThrow<InvalidArgumentsException> {
            request.toCommand()
        }

        // then
        exception.details.single().field shouldBe "sections.displayOrder"
    }

    "이력서 저장 요청의 같은 섹션 안 아이템 displayOrder가 중복되면 예외가 발생한다" {
        // given
        val request = SaveResumeRequest(
            targetJdId = null,
            template = ResumeTemplate.DEFAULT,
            status = ResumeStatusType.DRAFT,
            sections = listOf(
                SaveResumeSectionRequest(
                    sectionId = 200L,
                    type = ResumeSectionType.BASIC_INFO,
                    displayOrder = 1.0,
                    visible = true,
                    items = listOf(
                        basicInfoItem(displayOrder = 1.0),
                        basicInfoItem(displayOrder = 1.0),
                    ),
                ),
            ),
        )

        // when
        val exception = shouldThrow<InvalidArgumentsException> {
            request.toCommand()
        }

        // then
        exception.details.single().field shouldBe "sections.items.displayOrder"
    }

    "이력서 저장 요청의 섹션 타입과 아이템 payload 타입이 다르면 예외가 발생한다" {
        // given
        val request = SaveResumeRequest(
            targetJdId = null,
            template = ResumeTemplate.DEFAULT,
            status = ResumeStatusType.DRAFT,
            sections = listOf(
                SaveResumeSectionRequest(
                    sectionId = 200L,
                    type = ResumeSectionType.LANGUAGE,
                    displayOrder = 1.0,
                    visible = true,
                    items = listOf(basicInfoItem(displayOrder = 1.0)),
                ),
            ),
        )

        // when
        val exception = shouldThrow<InvalidArgumentsException> {
            request.toCommand()
        }

        // then
        exception.details.single().field shouldBe "sections.items.payload"
    }

})

private fun saveRequest() = SaveResumeRequest(
    targetJdId = null,
    template = ResumeTemplate.DEFAULT,
    status = ResumeStatusType.DRAFT,
    sections = listOf(
        SaveResumeSectionRequest(
            sectionId = 200L,
            type = ResumeSectionType.BASIC_INFO,
            displayOrder = 10.0,
            visible = true,
            items = listOf(
                SaveResumeSectionItemRequest(
                    itemId = 300L,
                    displayOrder = 1.0,
                    visible = true,
                    payload = ResumeSectionItemPayloadRequest(
                        basicInfo = ResumeBasicInfoPayloadRequest(
                            name = "홍길동",
                            email = "hong@example.com",
                            phone = "010-0000-0000",
                        ),
                        coreSkill = null,
                        career = null,
                        experience = null,
                        education = null,
                        award = null,
                        certificate = null,
                        language = null,
                        skill = null,
                    ),
                ),
            ),
        ),
    ),
)

private fun basicInfoSection(displayOrder: Double) = SaveResumeSectionRequest(
    sectionId = 200L,
    type = ResumeSectionType.BASIC_INFO,
    displayOrder = displayOrder,
    visible = true,
    items = listOf(basicInfoItem(displayOrder = 1.0)),
)

private fun languageSection(displayOrder: Double) = SaveResumeSectionRequest(
    sectionId = 300L,
    type = ResumeSectionType.LANGUAGE,
    displayOrder = displayOrder,
    visible = true,
    items = listOf(languageItem(displayOrder = 1.0)),
)

private fun basicInfoItem(displayOrder: Double) = SaveResumeSectionItemRequest(
    itemId = 300L,
    displayOrder = displayOrder,
    visible = true,
    payload = ResumeSectionItemPayloadRequest(
        basicInfo = ResumeBasicInfoPayloadRequest(
            name = "홍길동",
            email = "hong@example.com",
            phone = "010-0000-0000",
        ),
        coreSkill = null,
        career = null,
        experience = null,
        education = null,
        award = null,
        certificate = null,
        language = null,
        skill = null,
    ),
)

private fun languageItem(displayOrder: Double) = SaveResumeSectionItemRequest(
    itemId = 400L,
    displayOrder = displayOrder,
    visible = true,
    payload = ResumeSectionItemPayloadRequest(
        basicInfo = null,
        coreSkill = null,
        career = null,
        experience = null,
        education = null,
        award = null,
        certificate = null,
        language = ResumeLanguagePayloadRequest(
            examName = "TOEIC",
            scoreOrGrade = "900",
            acquiredAt = LocalDate.of(2026, 1, 1),
        ),
        skill = null,
    ),
)

private fun languageSaveRequest() = SaveResumeRequest(
    targetJdId = null,
    template = ResumeTemplate.DEFAULT,
    status = ResumeStatusType.DRAFT,
    sections = listOf(
        SaveResumeSectionRequest(
            sectionId = 200L,
            type = ResumeSectionType.LANGUAGE,
            displayOrder = 10.0,
            visible = true,
            items = listOf(
                SaveResumeSectionItemRequest(
                    itemId = 300L,
                    displayOrder = 1.0,
                    visible = true,
                    payload = ResumeSectionItemPayloadRequest(
                        basicInfo = null,
                        coreSkill = null,
                        career = null,
                        experience = null,
                        education = null,
                        award = null,
                        certificate = null,
                        language = ResumeLanguagePayloadRequest(
                            examName = "TOEIC",
                            scoreOrGrade = "900",
                            acquiredAt = LocalDate.of(2026, 1, 1),
                        ),
                        skill = null,
                    ),
                ),
            ),
        ),
    ),
)
