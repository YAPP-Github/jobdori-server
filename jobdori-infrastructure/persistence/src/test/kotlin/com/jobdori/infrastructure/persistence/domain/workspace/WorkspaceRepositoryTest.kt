package com.jobdori.infrastructure.persistence.domain.workspace

import com.jobdori.core.domain.workspace.WorkspaceFixture
import com.jobdori.core.domain.workspace.repository.WorkspaceRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.workspace.entity.WorkspaceEntity
import com.jobdori.infrastructure.persistence.domain.workspace.repository.WorkspaceJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

@IntegrationTest
class WorkspaceRepositoryTest(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceJpaRepository: WorkspaceJpaRepository,
) : StringSpec({

    afterEach {
        workspaceJpaRepository.deleteAll()
    }

    "워크스페이스 publicId로 조회한다" {
        // given
        val entity = workspaceJpaRepository.save(
            WorkspaceEntity.from(
                WorkspaceFixture.create(
                    ownerUserId = 10L,
                ),
            ),
        )

        // when & then
        workspaceRepository.findByPublicId(entity.publicId) shouldBe entity.toDomain()
    }

    "워크스페이스 publicId로 조회할 때 없으면 null을 반환한다" {
        // when & then
        workspaceRepository.findByPublicId("3f5c9d79-2255-4b76-bd31-013cd01d49d6") shouldBe null
    }

    "소유자 유저 ID로 워크스페이스 목록을 조회한다" {
        // given
        val entity = workspaceJpaRepository.save(
            WorkspaceEntity.from(
                WorkspaceFixture.create(
                    ownerUserId = 10L,
                ),
            ),
        )

        // when & then
        workspaceRepository.findAllByOwnerUserId(10L) shouldBe listOf(entity.toDomain())
    }

    "워크스페이스를 저장한다" {
        // when
        val saved = workspaceRepository.save(WorkspaceFixture.create(ownerUserId = 10L))

        // then
        val workspaces = workspaceJpaRepository.findAll()
        workspaces shouldHaveSize 1
        workspaces[0].also {
            it.id shouldBe saved.id
            it.publicId shouldBe saved.publicId
            it.ownerUserId shouldBe saved.ownerUserId
        }
    }

})
