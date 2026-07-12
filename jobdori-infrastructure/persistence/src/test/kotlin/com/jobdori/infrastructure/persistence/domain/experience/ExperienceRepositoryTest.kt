package com.jobdori.infrastructure.persistence.domain.experience

import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceFixture
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceEntity
import com.jobdori.infrastructure.persistence.domain.experience.repository.ExperienceJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

@IntegrationTest
class ExperienceRepositoryTest(
    private val experienceRepository: ExperienceRepository,
    private val experienceJpaRepository: ExperienceJpaRepository,
) : StringSpec({

    afterEach {
        experienceJpaRepository.deleteAll()
    }

    "경험을 저장한다" {
        // when
        val saved = experienceRepository.save(
            ExperienceFixture.create(
                id = 0L,
                workspaceId = 10L,
                projectId = 100L,
                contents = ExperienceContents.star(
                    situation = "상황",
                    task = "과제",
                    action = "행동",
                    result = "결과",
                ),
            ),
        )

        // then
        val experiences = experienceJpaRepository.findAll()
        experiences shouldHaveSize 1
        experiences[0].toDomain().also {
            it.id shouldBe saved.id
            it.workspaceId shouldBe saved.workspaceId
            it.projectId shouldBe saved.projectId
            it.contents shouldBe saved.contents
            it.status shouldBe saved.status
        }
    }

    "ID와 워크스페이스 ID로 활성 경험을 조회한다" {
        // given
        val active = experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 100L)),
        )
        val deleted = experienceJpaRepository.save(
            ExperienceEntity.from(
                ExperienceFixture.create(
                    workspaceId = 10L,
                    projectId = 100L,
                    status = ExperienceStatus.DELETED,
                ),
            ),
        )

        // when & then
        experienceRepository.findByIdAndWorkspaceId(active.id, 10L)?.id shouldBe active.id
        experienceRepository.findByIdAndWorkspaceId(deleted.id, 10L).shouldBeNull()
        experienceRepository.findByIdAndWorkspaceId(active.id, 20L).shouldBeNull()
    }

    "워크스페이스의 활성 경험 목록을 ID 역순 slice로 조회한다" {
        // given
        val first = experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 100L)),
        )
        val second = experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 100L)),
        )
        val third = experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 200L)),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 20L, projectId = 100L)),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(
                ExperienceFixture.create(
                    workspaceId = 10L,
                    projectId = 100L,
                    status = ExperienceStatus.DELETED,
                ),
            ),
        )

        // when
        val firstPage = experienceRepository.findAllByWorkspaceId(
            workspaceId = 10L,
            cursorId = null,
            size = 2,
        )
        val nextPage = experienceRepository.findAllByWorkspaceId(
            workspaceId = 10L,
            cursorId = second.id,
            size = 2,
        )

        // then
        firstPage.map { it.id } shouldContainExactly listOf(third.id, second.id)
        nextPage.map { it.id } shouldContainExactly listOf(first.id)
    }

    "프로젝트의 활성 경험 목록을 ID 역순 slice로 조회한다" {
        // given
        val first = experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 100L)),
        )
        val second = experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 100L)),
        )
        val third = experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 100L)),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 200L)),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 20L, projectId = 100L)),
        )

        // when
        val firstPage = experienceRepository.findAllByWorkspaceIdAndProjectId(
            workspaceId = 10L,
            projectId = 100L,
            cursorId = null,
            size = 2,
        )
        val nextPage = experienceRepository.findAllByWorkspaceIdAndProjectId(
            workspaceId = 10L,
            projectId = 100L,
            cursorId = second.id,
            size = 2,
        )

        // then
        firstPage.map { it.id } shouldContainExactly listOf(third.id, second.id)
        nextPage.map { it.id } shouldContainExactly listOf(first.id)
    }

    "JDSL custom repository로 프로젝트별 활성 경험 개수를 조회한다" {
        // given
        experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 100L)),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 100L)),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 10L, projectId = 200L)),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(
                ExperienceFixture.create(
                    workspaceId = 10L,
                    projectId = 100L,
                    status = ExperienceStatus.DELETED,
                ),
            ),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(ExperienceFixture.create(workspaceId = 20L, projectId = 100L)),
        )

        // when
        val counts = experienceJpaRepository.countByWorkspaceIdAndProjectIdsAndStatus(
            workspaceId = 10L,
            projectIds = listOf(100L, 200L, 300L),
            status = ExperienceStatus.ACTIVE,
        )

        // then
        counts shouldBe mapOf(
            100L to 2L,
            200L to 1L,
        )
    }

    "프로젝트별 경험 개수 조회 요청이 비어 있으면 빈 Map을 반환한다" {
        // when & then
        experienceRepository.countByWorkspaceIdAndProjectIds(
            workspaceId = 10L,
            projectIds = emptyList(),
        ) shouldContainExactly emptyMap()
    }

    "워크스페이스 내 제목 또는 내용에 검색어가 포함된 활성 경험을 조회한다" {
        // given
        val titleMatched = experienceJpaRepository.save(
            ExperienceEntity.from(
                ExperienceFixture.create(
                    workspaceId = 10L,
                    title = "Kotlin 성능 개선",
                    contents = ExperienceContents.free("코루틴 구조를 변경했다"),
                ),
            ),
        )
        val contentsMatched = experienceJpaRepository.save(
            ExperienceEntity.from(
                ExperienceFixture.create(
                    workspaceId = 10L,
                    title = "검색 API 추가",
                    contents = ExperienceContents.free("성능 병목을 줄였다"),
                ),
            ),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(
                ExperienceFixture.create(
                    workspaceId = 10L,
                    title = "삭제된 성능 개선",
                    contents = ExperienceContents.free("검색 대상이 아니다"),
                    status = ExperienceStatus.DELETED,
                ),
            ),
        )
        experienceJpaRepository.save(
            ExperienceEntity.from(
                ExperienceFixture.create(
                    workspaceId = 20L,
                    title = "다른 워크스페이스 성능 개선",
                    contents = ExperienceContents.free("검색 대상이 아니다"),
                ),
            ),
        )

        // when
        val result = experienceRepository.searchAllByWorkspaceId(
            workspaceId = 10L,
            keyword = "성능",
            cursorId = null,
            size = 10,
        )

        // then
        result.map { it.id } shouldContainExactly listOf(contentsMatched.id, titleMatched.id)
    }

})
