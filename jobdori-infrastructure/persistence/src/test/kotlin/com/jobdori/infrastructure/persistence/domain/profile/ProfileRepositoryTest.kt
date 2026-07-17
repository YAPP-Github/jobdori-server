package com.jobdori.infrastructure.persistence.domain.profile

import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.repository.ProfileRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.profile.repository.ProfileJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@IntegrationTest
class ProfileRepositoryTest(
    private val profileRepository: ProfileRepository,
    private val profileJpaRepository: ProfileJpaRepository,
) : StringSpec({

    afterEach {
        profileJpaRepository.deleteAll()
    }

    "프로필 개인정보를 암호화해서 저장하고 복호화해서 조회한다" {
        val profile = Profile(
            id = 0L,
            workspaceId = 10L,
            name = "홍길동",
            phone = "010-1234-5678",
            email = "hong@example.com",
            coreCompetency = "백엔드 개발",
        )

        val saved = profileRepository.save(profile)

        val entity = profileJpaRepository.findAll().single()
        entity.nameEncrypted shouldNotBe profile.name
        entity.phoneEncrypted shouldNotBe profile.phone
        entity.emailEncrypted shouldNotBe profile.email
        profileRepository.findByWorkspaceId(profile.workspaceId) shouldBe saved
    }
})
