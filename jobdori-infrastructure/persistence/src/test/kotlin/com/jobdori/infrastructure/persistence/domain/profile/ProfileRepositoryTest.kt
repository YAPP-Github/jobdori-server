package com.jobdori.infrastructure.persistence.domain.profile

import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.repository.ProfileRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.profile.repository.ProfileJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.jdbc.core.JdbcTemplate

@IntegrationTest
class ProfileRepositoryTest(
    private val profileRepository: ProfileRepository,
    private val profileJpaRepository: ProfileJpaRepository,
    private val jdbcTemplate: JdbcTemplate,
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

        profileJpaRepository.flush()
        val encrypted = jdbcTemplate.queryForMap(
            "select name_encrypted, phone_encrypted, email_encrypted from profile_v1 where workspace_id = ?",
            profile.workspaceId,
        )
        encrypted["name_encrypted"] shouldNotBe profile.name
        encrypted["phone_encrypted"] shouldNotBe profile.phone
        encrypted["email_encrypted"] shouldNotBe profile.email
        profileRepository.findByWorkspaceId(profile.workspaceId) shouldBe saved
    }

})
