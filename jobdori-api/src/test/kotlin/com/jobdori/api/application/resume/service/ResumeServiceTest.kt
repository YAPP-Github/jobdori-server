package com.jobdori.api.application.resume.service

import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.api.application.resume.dto.ResumeOptimizationMode
import com.jobdori.api.application.resume.dto.request.ResumeBasicInfoPayloadRequest
import com.jobdori.api.application.resume.dto.request.CreateResumeRequest
import com.jobdori.api.application.resume.dto.request.ResumeLanguagePayloadRequest
import com.jobdori.api.application.resume.dto.request.ResumeExperiencePayloadRequest
import com.jobdori.api.application.resume.dto.request.ResumeSectionItemPayloadRequest
import com.jobdori.api.application.resume.dto.request.SaveResumeRequest
import com.jobdori.api.application.resume.dto.request.SaveResumeSectionItemRequest
import com.jobdori.api.application.resume.dto.request.SaveResumeSectionRequest
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.model.SliceResult
import com.jobdori.core.application.credit.CreditService
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.resume.ResumeExperiencePolishService
import com.jobdori.core.domain.credit.CreditFeature
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.ResumeDetailSection
import com.jobdori.core.domain.resume.ResumeFixture
import com.jobdori.core.domain.resume.ResumeLanguagePayload
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeSectionFixture
import com.jobdori.core.domain.resume.ResumeSectionItemFixture
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.ProfileSections
import com.jobdori.core.domain.resume.service.ResumeCreator
import com.jobdori.core.domain.resume.service.ResumeModifier
import com.jobdori.core.domain.resume.service.ResumeReader
import com.jobdori.core.domain.resume.service.ResumeRemover
import com.jobdori.core.domain.resume.service.ResumeSectionItemInitializer
import com.jobdori.core.domain.profile.service.ProfileReader
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
    val creditService = mockk<CreditService>(relaxed = true)
    val resumeCreator = mockk<ResumeCreator>()
    val resumeReader = mockk<ResumeReader>()
    val resumeRemover = mockk<ResumeRemover>()
    val resumeModifier = mockk<ResumeModifier>()
    val getJdService = mockk<GetJdService>()
    val profileReader = mockk<ProfileReader>()
    val resumeSectionItemInitializer = mockk<ResumeSectionItemInitializer>()
    val resumeExperiencePolishService = mockk<ResumeExperiencePolishService>()
    val resumeService = ResumeService(
        workspaceAccessValidationService = workspaceAccessValidationService,
        creditService = creditService,
        resumeCreator = resumeCreator,
        resumeReader = resumeReader,
        resumeRemover = resumeRemover,
        resumeModifier = resumeModifier,
        getJdService = getJdService,
        profileReader = profileReader,
        resumeSectionItemInitializer = resumeSectionItemInitializer,
        resumeExperiencePolishService = resumeExperiencePolishService,
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

    "프로필 초기화 옵션으로 이력서를 생성하면 프로필 섹션을 command에 포함한다" {
        val request = CreateResumeRequest(
            targetJdId = null,
            template = ResumeTemplate.DEFAULT,
            status = ResumeStatusType.COMPLETED,
            optimizationMode = ResumeOptimizationMode.NONE,
            sections = listOf(
                SaveResumeSectionRequest(
                    sectionId = null,
                    type = ResumeSectionType.BASIC_INFO,
                    displayOrder = 10.0,
                    visible = true,
                    items = emptyList(),
                    useDefaultItems = true,
                ),
            ),
        )
        val profile = Profile(1L, 1L, "홍길동", null, null, null)
        val profileDetail = ProfileDetail(
            profile = profile,
            sections = ProfileSections(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
        )
        val initializedItems = saveRequest().toCommand().sections.single().items
        val createdDetail = ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L),
            sections = emptyList(),
        )
        every { profileReader.getOrCreateProfile(1L) } returns profile
        every { profileReader.getDetail(profile) } returns profileDetail
        every {
            resumeSectionItemInitializer.initializeItems(profileDetail, ResumeSectionType.BASIC_INFO)
        } returns initializedItems
        every { resumeCreator.create(workspaceId = 1L, command = any()) } returns createdDetail

        resumeService.createResume(10L, "workspace-id", request)

        verify(exactly = 1) {
            resumeCreator.create(
                workspaceId = 1L,
                command = withArg {
                    it.status shouldBe ResumeStatus.COMPLETED
                    it.sections.single().items shouldBe initializedItems
                },
            )
        }
        verify(exactly = 0) {
            getJdService.getJd(workspaceId = 1L, publicId = any())
        }
        verify(exactly = 0) { creditService.consume(any(), any()) }
    }

    "targetJdId가 있으면 JD public ID를 내부 ID로 변환해 생성한다" {
        val request = CreateResumeRequest(
            targetJdId = "jd-public-id",
            template = ResumeTemplate.DEFAULT,
            status = ResumeStatusType.DRAFT,
            sections = listOf(basicInfoSection(displayOrder = 10.0)),
        )
        val createdDetail = ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L, targetJdId = 20L),
            sections = emptyList(),
        )
        every { getJdService.getJd(workspaceId = 1L, publicId = "jd-public-id") } returns targetJd()
        every { resumeCreator.create(workspaceId = 1L, command = any()) } returns createdDetail

        resumeService.createResume(10L, "workspace-id", request)

        verify(exactly = 1) {
            resumeCreator.create(
                workspaceId = 1L,
                command = withArg { it.targetJdId shouldBe 20L },
            )
        }
    }

    "생성 요청도 JOB_SPECIFIC 모드이면 경험 contents만 첨삭한다" {
        val jd = targetJd()
        val saveRequest = experienceSaveRequest(ResumeOptimizationMode.JOB_SPECIFIC, jd.publicId)
        val request = CreateResumeRequest(
            targetJdId = saveRequest.targetJdId,
            template = saveRequest.template,
            status = saveRequest.status,
            sections = saveRequest.sections,
            optimizationMode = saveRequest.optimizationMode,
        )
        every { getJdService.getJd(1L, jd.publicId) } returns jd
        every { resumeExperiencePolishService.polish(listOf("원본 내용"), jd) } returns listOf("생성 첨삭 내용")
        every { resumeCreator.create(1L, any()) } returns ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L, targetJdId = jd.id),
            sections = emptyList(),
        )

        resumeService.createResume(10L, "workspace-id", request)

        verify {
            resumeCreator.create(1L, withArg<ResumeSaveCommand> { command ->
                val payload = command.sections.single().items.single().payload as ResumeExperiencePayload
                payload.name shouldBe "프로젝트"
                payload.role shouldBe "백엔드 개발"
                payload.contents shouldBe "생성 첨삭 내용"
            })
        }
        verify(exactly = 1) { creditService.consume(10L, CreditFeature.RESUME_DRAFT) }
    }

    "기본 아이템 생성과 직접 입력한 items를 함께 지정하면 예외가 발생한다" {
        val section = basicInfoSection(displayOrder = 10.0).copy(useDefaultItems = true)
        val request = CreateResumeRequest(
            targetJdId = null,
            template = ResumeTemplate.DEFAULT,
            status = ResumeStatusType.DRAFT,
            sections = listOf(section),
        )

        val exception = shouldThrow<InvalidArgumentsException> { request.toCommand() }

        exception.details.single().field shouldBe "sections.items"
    }

    "기본 아이템이 없으면 해당 섹션을 생성하지 않는다" {
        val request = CreateResumeRequest(
            targetJdId = null,
            template = ResumeTemplate.DEFAULT,
            status = ResumeStatusType.DRAFT,
            optimizationMode = ResumeOptimizationMode.NONE,
            sections = listOf(
                SaveResumeSectionRequest(
                    sectionId = null,
                    type = ResumeSectionType.SKILL,
                    displayOrder = 100.0,
                    visible = true,
                    items = emptyList(),
                    useDefaultItems = true,
                ),
            ),
        )
        val profile = Profile(1L, 1L, "홍길동", null, null, null)
        val profileDetail = ProfileDetail(
            profile = profile,
            sections = ProfileSections(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
        )
        val createdDetail = ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L),
            sections = emptyList(),
        )
        every { profileReader.getOrCreateProfile(1L) } returns profile
        every { profileReader.getDetail(profile) } returns profileDetail
        every {
            resumeSectionItemInitializer.initializeItems(profileDetail, ResumeSectionType.SKILL)
        } returns emptyList()
        every { resumeCreator.create(workspaceId = 1L, command = any()) } returns createdDetail

        resumeService.createResume(10L, "workspace-id", request)

        verify(exactly = 1) {
            resumeCreator.create(
                workspaceId = 1L,
                command = withArg { it.sections shouldBe emptyList() },
            )
        }
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

    "JOB_SPECIFIC 모드는 JD를 기준으로 경험 contents만 첨삭해서 저장한다" {
        val jd = targetJd()
        val request = experienceSaveRequest(optimizationMode = ResumeOptimizationMode.JOB_SPECIFIC, targetJdId = jd.publicId)
        val detail = ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L),
            sections = emptyList(),
        )
        every { getJdService.getJd(workspaceId = 1L, publicId = jd.publicId) } returns jd
        every { resumeExperiencePolishService.polish(listOf("원본 내용"), jd) } returns listOf("첨삭 내용")
        every { resumeModifier.modifyDetail(1L, 100L, any()) } returns detail

        resumeService.modifyResume(10L, "workspace-id", 100L, request)

        verify(exactly = 1) {
            resumeModifier.modifyDetail(
                workspaceId = 1L,
                resumeId = 100L,
                command = withArg<ResumeSaveCommand> { command ->
                    command.targetJdId shouldBe 20L
                    val payload = command.sections.single().items.single().payload as ResumeExperiencePayload
                    payload.name shouldBe "프로젝트"
                    payload.role shouldBe "백엔드 개발"
                    payload.period shouldBe null
                    payload.contents shouldBe "첨삭 내용"
                },
            )
        }
    }

    "NONE 모드는 경험 contents를 첨삭하지 않고 저장한다" {
        val request = experienceSaveRequest(optimizationMode = ResumeOptimizationMode.NONE, targetJdId = null)
        every { resumeModifier.modifyDetail(1L, 100L, any()) } returns ResumeDetail(
            resume = ResumeFixture.create(id = 100L, workspaceId = 1L),
            sections = emptyList(),
        )

        resumeService.modifyResume(10L, "workspace-id", 100L, request)

        verify(exactly = 0) { resumeExperiencePolishService.polish(any(), any()) }
        verify {
            resumeModifier.modifyDetail(1L, 100L, withArg<ResumeSaveCommand> { command ->
                val payload = command.sections.single().items.single().payload as ResumeExperiencePayload
                payload.contents shouldBe "원본 내용"
            })
        }
    }

    "JOB_SPECIFIC 모드에 대상 JD가 없으면 저장하지 않는다" {
        val request = experienceSaveRequest(optimizationMode = ResumeOptimizationMode.JOB_SPECIFIC, targetJdId = null)

        shouldThrow<InvalidArgumentsException> {
            resumeService.modifyResume(10L, "workspace-id", 100L, request)
        }

        verify(exactly = 0) { resumeModifier.modifyDetail(any(), any(), any()) }
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

    "이력서 수를 상태별 응답으로 변환한다" {
        // given
        every {
            resumeReader.countResumes(
                workspaceId = 1L,
                statuses = listOf(ResumeStatus.COMPLETED, ResumeStatus.DRAFT),
            )
        } returns mapOf(
            ResumeStatus.COMPLETED to 2L,
            ResumeStatus.DRAFT to 1L,
        )

        // when
        val response = resumeService.countResumes(
            userId = 10L,
            workspaceId = "workspace-id",
        )

        // then
        response.map { it.status to it.count } shouldBe listOf(
            ResumeStatusType.COMPLETED to 2L,
            ResumeStatusType.DRAFT to 1L,
        )
        verify(exactly = 1) {
            resumeReader.countResumes(
                workspaceId = 1L,
                statuses = listOf(ResumeStatus.COMPLETED, ResumeStatus.DRAFT),
            )
        }
    }

    "이력서 목록의 대상 JD를 한 번에 조회한다" {
        // given
        every {
            resumeReader.getResumes(
                workspaceId = 1L,
                statuses = listOf(ResumeStatus.COMPLETED, ResumeStatus.DRAFT),
                cursor = null,
                size = 10,
            )
        } returns SliceResult(
            items = listOf(
                ResumeFixture.create(id = 100L, workspaceId = 1L, targetJdId = 20L),
                ResumeFixture.create(id = 101L, workspaceId = 1L, targetJdId = 20L),
            ),
            nextCursor = "101",
        )
        every {
            getJdService.getJds(workspaceId = 1L, ids = listOf(20L, 20L))
        } returns listOf(targetJd())

        // when
        val response = resumeService.getResumes(
            userId = 10L,
            workspaceId = "workspace-id",
            statuses = null,
            cursor = null,
            size = 10,
            includeTargetJd = true,
        )

        // then
        response.resumes.map { it.targetJd?.jdId } shouldBe listOf("jd-public-id", "jd-public-id")
        response.cursor.nextCursor shouldBe "101"
        response.cursor.hasNext shouldBe true
        verify(exactly = 1) {
            getJdService.getJds(workspaceId = 1L, ids = listOf(20L, 20L))
        }
    }

    "이력서 목록에 targetJd를 요청하지 않으면 JD를 조회하지 않는다" {
        // given
        every {
            resumeReader.getResumes(
                workspaceId = 1L,
                statuses = listOf(ResumeStatus.COMPLETED, ResumeStatus.DRAFT),
                cursor = "102",
                size = 10,
            )
        } returns SliceResult(
            items = listOf(ResumeFixture.create(id = 102L, workspaceId = 1L, targetJdId = 99L)),
            nextCursor = null,
        )

        // when
        val response = resumeService.getResumes(
            userId = 10L,
            workspaceId = "workspace-id",
            statuses = null,
            cursor = "102",
            size = 10,
            includeTargetJd = false,
        )

        // then
        response.resumes.single().targetJd shouldBe null
        verify(exactly = 0) {
            getJdService.getJds(workspaceId = 1L, ids = listOf(99L))
        }
    }

    "이력서의 targetJdId로 대상 JD를 조회해 응답한다" {
        // given
        every {
            resumeReader.getResume(workspaceId = 1L, resumeId = 100L)
        } returns ResumeFixture.create(id = 100L, workspaceId = 1L, targetJdId = 20L)
        every {
            getJdService.getJd(workspaceId = 1L, id = 20L)
        } returns targetJd()

        // when
        val response = resumeService.getResume(
            userId = 10L,
            workspaceId = "workspace-id",
            resumeId = 100L,
            includeSections = false,
            includeSectionItems = false,
            includeTargetJd = true,
        )

        // then
        response.targetJd?.jdId shouldBe "jd-public-id"
        response.targetJd?.companyName shouldBe "잡도리"
    }

    "이력서 상세의 targetJd 하위에서 insight를 응답한다" {
        // given
        every {
            resumeReader.getResume(workspaceId = 1L, resumeId = 103L)
        } returns ResumeFixture.create(id = 103L, workspaceId = 1L, targetJdId = 20L)
        every {
            getJdService.getJd(workspaceId = 1L, id = 20L)
        } returns targetJd()

        // when
        val response = resumeService.getResume(
            userId = 10L,
            workspaceId = "workspace-id",
            resumeId = 103L,
            includeSections = false,
            includeSectionItems = false,
            includeTargetJd = true,
        )

        // then
        response.targetJd?.insight?.strategy shouldBe "지원 전략"
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

private fun targetJd() = Jd(
    id = 20L,
    publicId = "jd-public-id",
    workspaceId = 1L,
    sourceUrl = null,
    companyName = "잡도리",
    positionTitle = "백엔드 개발자",
    companyIntro = "",
    responsibilities = emptyList(),
    requiredExperiences = emptyList(),
    preferredExperiences = emptyList(),
    hiringProcess = emptyList(),
    coreCompetencies = emptyList(),
    keyPoints = "핵심 포인트",
    strategy = "지원 전략",
)

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

private fun experienceSaveRequest(optimizationMode: ResumeOptimizationMode, targetJdId: String?) = SaveResumeRequest(
    targetJdId = targetJdId,
    template = ResumeTemplate.DEFAULT,
    status = ResumeStatusType.DRAFT,
    optimizationMode = optimizationMode,
    sections = listOf(
        SaveResumeSectionRequest(
            sectionId = 200L,
            type = ResumeSectionType.EXPERIENCE,
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
                        experience = ResumeExperiencePayloadRequest(
                            name = "프로젝트",
                            role = "백엔드 개발",
                            period = null,
                            contents = "원본 내용",
                        ),
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
