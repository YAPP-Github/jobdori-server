package com.jobdori.infrastructure.persistence.domain.resume

import com.jobdori.core.domain.resume.ResumeBasicInfoPayload
import com.jobdori.core.domain.resume.ResumeCareerPayload
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.resume.repository.ResumeRepository
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionItemSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionSaveCommand
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeEntity
import com.jobdori.infrastructure.persistence.domain.resume.repository.ResumeJpaRepository
import com.jobdori.infrastructure.persistence.domain.resume.repository.ResumeSectionItemJpaRepository
import com.jobdori.infrastructure.persistence.domain.resume.repository.ResumeSectionJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain

@IntegrationTest
class ResumeRepositoryTest(
    private val resumeRepository: ResumeRepository,
    private val resumeJpaRepository: ResumeJpaRepository,
    private val sectionJpaRepository: ResumeSectionJpaRepository,
    private val sectionItemJpaRepository: ResumeSectionItemJpaRepository,
) : StringSpec({

    afterEach {
        sectionItemJpaRepository.deleteAll()
        sectionJpaRepository.deleteAll()
        resumeJpaRepository.deleteAll()
    }

    "이력서 상세를 생성하고 섹션과 아이템을 순서대로 조회한다" {
        // when
        val detail = resumeRepository.createDetail(
            workspaceId = 10L,
            command = saveCommand(
                sections = listOf(
                    sectionCommand(type = ResumeSectionType.CAREER, displayOrder = "20"),
                    sectionCommand(type = ResumeSectionType.BASIC_INFO, displayOrder = "10"),
                ),
            ),
        )

        // then
        detail.resume.status shouldBe ResumeStatus.DRAFT
        detail.sections.map { it.section.type } shouldContainExactly listOf(
            ResumeSectionType.BASIC_INFO,
            ResumeSectionType.CAREER,
        )
        detail.sections.flatMap { it.items }.map { it.payload.type } shouldContainExactly listOf(
            ResumeSectionType.BASIC_INFO,
            ResumeSectionType.CAREER,
        )
    }

    "이력서 기본정보 개인정보를 암호화해서 저장하고 복호화해서 조회한다" {
        val detail = resumeRepository.createDetail(
            workspaceId = 10L,
            command = saveCommand(),
        )

        val storedPayload = sectionItemJpaRepository.findAll().single().payload
        storedPayload shouldNotContain "홍길동"
        storedPayload shouldNotContain "hong@example.com"
        storedPayload shouldNotContain "010-0000-0000"
        detail.sections.single().items.single().payload shouldBe ResumeBasicInfoPayload(
            name = "홍길동",
            email = "hong@example.com",
            phone = "010-0000-0000",
        )
    }

    "이력서 상세 수정 시 요청에 없는 섹션과 아이템을 삭제하고 요청 스냅샷을 저장한다" {
        // given
        val created = resumeRepository.createDetail(
            workspaceId = 10L,
            command = saveCommand(
                sections = listOf(
                    sectionCommand(type = ResumeSectionType.BASIC_INFO, displayOrder = "10"),
                    sectionCommand(type = ResumeSectionType.CAREER, displayOrder = "20"),
                ),
            ),
        )
        val basicInfoSection = created.sections.first { it.section.type == ResumeSectionType.BASIC_INFO }
        val basicInfoItem = basicInfoSection.items.single()

        // when
        val modified = resumeRepository.modifyDetail(
            id = created.resume.id,
            workspaceId = 10L,
            command = saveCommand(
                status = ResumeStatus.COMPLETED,
                sections = listOf(
                    sectionCommand(
                        sectionId = basicInfoSection.section.id,
                        itemId = basicInfoItem.id,
                        type = ResumeSectionType.BASIC_INFO,
                        displayOrder = "30",
                        visible = false,
                    ),
                ),
            ),
        )

        // then
        requireNotNull(modified)
        modified.resume.status shouldBe ResumeStatus.COMPLETED
        modified.sections shouldHaveSize 1
        modified.sections.single().section.type shouldBe ResumeSectionType.BASIC_INFO
        modified.sections.single().section.displayOrder shouldBe 30.0
        modified.sections.single().section.visible shouldBe false
        modified.sections.single().items.single().id shouldBe basicInfoItem.id
        sectionJpaRepository.findAll() shouldHaveSize 1
        sectionItemJpaRepository.findAll() shouldHaveSize 1
    }

    "없는 이력서 상세 수정은 null을 반환한다" {
        // when & then
        resumeRepository.modifyDetail(
            id = 999L,
            workspaceId = 10L,
            command = saveCommand(),
        ).shouldBeNull()
    }

    "워크스페이스의 이력서 수를 상태별로 조회한다" {
        // given
        resumeJpaRepository.saveAll(
            listOf(
                resumeEntity(workspaceId = 10L, status = ResumeStatus.COMPLETED),
                resumeEntity(workspaceId = 10L, status = ResumeStatus.COMPLETED),
                resumeEntity(workspaceId = 10L, status = ResumeStatus.DRAFT),
                resumeEntity(workspaceId = 10L, status = ResumeStatus.DELETED),
                resumeEntity(workspaceId = 20L, status = ResumeStatus.COMPLETED),
            ),
        )

        // when
        val counts = resumeRepository.countByWorkspaceIdAndStatuses(
            workspaceId = 10L,
            statuses = listOf(ResumeStatus.COMPLETED, ResumeStatus.DRAFT),
        )

        // then
        counts shouldBe mapOf(
            ResumeStatus.COMPLETED to 2L,
            ResumeStatus.DRAFT to 1L,
        )
    }

})

private fun saveCommand(
    status: ResumeStatus = ResumeStatus.DRAFT,
    sections: List<ResumeSectionSaveCommand> = listOf(sectionCommand()),
) = ResumeSaveCommand(
    targetJdId = null,
    template = ResumeTemplate.DEFAULT,
    status = status,
    sections = sections,
)

private fun sectionCommand(
    sectionId: Long? = null,
    itemId: Long? = null,
    type: ResumeSectionType = ResumeSectionType.BASIC_INFO,
    displayOrder: String = "10",
    visible: Boolean = true,
) = ResumeSectionSaveCommand(
    sectionId = sectionId,
    type = type,
    displayOrder = displayOrder.toDouble(),
    visible = visible,
    items = listOf(
        ResumeSectionItemSaveCommand(
            itemId = itemId,
            payload = when (type) {
                ResumeSectionType.CAREER -> ResumeCareerPayload(
                    companyName = "회사",
                    role = "백엔드",
                    period = null,
                    contents = "경력 내용",
                )

                else -> ResumeBasicInfoPayload(
                    name = "홍길동",
                    email = "hong@example.com",
                    phone = "010-0000-0000",
                )
            },
            displayOrder = 1.0,
            visible = visible,
        ),
    ),
)

private fun resumeEntity(
    workspaceId: Long,
    status: ResumeStatus,
) = ResumeEntity(
    workspaceId = workspaceId,
    targetJdId = null,
    template = ResumeTemplate.DEFAULT,
    status = status,
)
