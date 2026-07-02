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

@IntegrationTest
class ExperienceRepositoryTest(
    private val experienceRepository: ExperienceRepository,
    private val experienceJpaRepository: ExperienceJpaRepository,
) : StringSpec({

    afterEach {
        experienceJpaRepository.deleteAll()
    }

    "워크스페이스 내 제목에 검색어가 포함된 활성 경험을 조회한다" {
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
        experienceJpaRepository.save(
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
        result.map { it.id } shouldContainExactly listOf(titleMatched.id)
    }

})
