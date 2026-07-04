package com.jobdori.infrastructure.persistence.domain.experience

import com.jobdori.core.domain.experience.ExperienceProjectFixture
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceProjectEntity
import com.jobdori.infrastructure.persistence.domain.experience.repository.ExperienceProjectJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

@IntegrationTest
class ExperienceProjectRepositoryTest(
    private val experienceProjectRepository: ExperienceProjectRepository,
    private val experienceProjectJpaRepository: ExperienceProjectJpaRepository,
) : StringSpec({

    afterEach {
        experienceProjectJpaRepository.deleteAll()
    }

    "경험 프로젝트를 저장한다" {
        // when
        val saved = experienceProjectRepository.save(
            ExperienceProjectFixture.create(
                id = 0L,
                workspaceId = 10L,
                name = "프로젝트",
                summary = "요약",
            ),
        )

        // then
        val projects = experienceProjectJpaRepository.findAll()
        projects shouldHaveSize 1
        projects[0].toDomain().also {
            it.id shouldBe saved.id
            it.workspaceId shouldBe saved.workspaceId
            it.name shouldBe saved.name
            it.summary shouldBe saved.summary
            it.status shouldBe saved.status
        }
    }

    "ID와 워크스페이스 ID로 활성 경험 프로젝트를 조회한다" {
        // given
        val active = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(ExperienceProjectFixture.create(workspaceId = 10L)),
        )
        val deleted = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(
                ExperienceProjectFixture.create(
                    workspaceId = 10L,
                    status = ExperienceProjectStatus.DELETED,
                ),
            ),
        )

        // when & then
        experienceProjectRepository.findByIdAndWorkspaceId(active.id, 10L)?.id shouldBe active.id
        experienceProjectRepository.findByIdAndWorkspaceId(deleted.id, 10L).shouldBeNull()
        experienceProjectRepository.findByIdAndWorkspaceId(active.id, 20L).shouldBeNull()
    }

    "여러 ID와 워크스페이스 ID로 활성 경험 프로젝트를 조회한다" {
        // given
        val project1 = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(ExperienceProjectFixture.create(workspaceId = 10L)),
        )
        val project2 = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(ExperienceProjectFixture.create(workspaceId = 10L)),
        )
        val otherWorkspace = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(ExperienceProjectFixture.create(workspaceId = 20L)),
        )
        val deleted = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(
                ExperienceProjectFixture.create(
                    workspaceId = 10L,
                    status = ExperienceProjectStatus.DELETED,
                ),
            ),
        )

        // when
        val projects = experienceProjectRepository.findAllByIdsAndWorkspaceId(
            ids = listOf(project1.id, project2.id, otherWorkspace.id, deleted.id),
            workspaceId = 10L,
        )

        // then
        projects.map { it.id } shouldContainExactlyInAnyOrder listOf(project1.id, project2.id)
    }

    "워크스페이스의 활성 경험 프로젝트 목록을 ID 역순 slice로 조회한다" {
        // given
        val first = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(ExperienceProjectFixture.create(workspaceId = 10L)),
        )
        val second = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(ExperienceProjectFixture.create(workspaceId = 10L)),
        )
        val third = experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(ExperienceProjectFixture.create(workspaceId = 10L)),
        )
        experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(ExperienceProjectFixture.create(workspaceId = 20L)),
        )
        experienceProjectJpaRepository.save(
            ExperienceProjectEntity.from(
                ExperienceProjectFixture.create(
                    workspaceId = 10L,
                    status = ExperienceProjectStatus.DELETED,
                ),
            ),
        )

        // when
        val firstPage = experienceProjectRepository.findAllByWorkspaceId(
            workspaceId = 10L,
            cursorId = null,
            size = 2,
        )
        val nextPage = experienceProjectRepository.findAllByWorkspaceId(
            workspaceId = 10L,
            cursorId = second.id,
            size = 2,
        )

        // then
        firstPage.map { it.id } shouldContainExactly listOf(third.id, second.id)
        nextPage.map { it.id } shouldContainExactly listOf(first.id)
    }

    "빈 ID 목록으로 조회하면 빈 List를 반환한다" {
        // when & then
        experienceProjectRepository.findAllByIdsAndWorkspaceId(
            ids = emptyList(),
            workspaceId = 10L,
        ) shouldContainExactly emptyList()
    }

})
