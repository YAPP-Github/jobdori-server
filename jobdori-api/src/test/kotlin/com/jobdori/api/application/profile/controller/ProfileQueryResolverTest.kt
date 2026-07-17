package com.jobdori.api.application.profile.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.profile.service.ProfileService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.common.model.Period
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.domain.keyword.KeywordType
import com.jobdori.core.domain.keyword.service.KeywordReader
import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.ProfileSections
import com.jobdori.core.domain.profile.section.Degree
import com.jobdori.core.domain.profile.section.Education
import com.jobdori.core.domain.profile.section.EducationStatus
import com.jobdori.core.domain.profile.section.ProfileSkill
import com.jobdori.core.domain.profile.section.SkillLevel
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity
import java.time.LocalDate

@GraphQLTest(ProfileQueryResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class ProfileQueryResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val profileService: ProfileService,
    @MockkBean
    private val keywordReader: KeywordReader,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
    }

    "이력서 기본 정보 프로필을 조회한다" {
        every {
            profileService.getProfile(userId = 1L, workspaceId = "workspace-id")
        } returns ProfileResponse.from(graphQlProfileDetail())

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  profile(workspaceId: "workspace-id") {
                    profileId
                    name
                    phone
                    email
                    coreCompetency
                    educations {
                      school
                      degree
                      status
                      period {
                        startAt
                        endAt
                      }
                    }
                    skills {
                      name
                      level
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("profile.profileId").entity<String>().isEqualTo("10")
            .path("profile.name").entity<String>().isEqualTo("잡도리")
            .path("profile.phone").entity<String>().isEqualTo("010-1111-2222")
            .path("profile.email").entity<String>().isEqualTo("rlajae14@gmail.com")
            .path("profile.coreCompetency").entity<String>().isEqualTo("핵심역량 내용")
            .path("profile.educations[0].school").entity<String>().isEqualTo("잡도리대학교")
            .path("profile.educations[0].degree").entity<String>().isEqualTo("BACHELOR")
            .path("profile.educations[0].status").entity<String>().isEqualTo("EXPECTED_GRADUATION")
            .path("profile.educations[0].period.startAt").entity<String>().isEqualTo("2020-03-01")
            .path("profile.skills[0].name").entity<String>().isEqualTo("GA4")
            .path("profile.skills[0].level").entity<String>().isEqualTo("HIGH")

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) { profileService.getProfile(userId = 1L, workspaceId = "workspace-id") }
    }

    "키워드 자동완성 제안을 조회한다" {
        every {
            keywordReader.suggest(type = KeywordType.LANGUAGE_TEST, keyword = "토익", size = 10)
        } returns listOf("토익", "토익스피킹")

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  suggestKeywords(type: LANGUAGE_TEST, keyword: "토익", size: 10)
                }
                """.trimIndent(),
            )
            .execute()
            .path("suggestKeywords").entity<List<String>>().isEqualTo(listOf("토익", "토익스피킹"))

        verify(exactly = 1) {
            keywordReader.suggest(type = KeywordType.LANGUAGE_TEST, keyword = "토익", size = 10)
        }
    }

})

private fun authenticatedTester(graphQlTester: GraphQlTester): GraphQlTester {
    val builder = graphQlTester.mutate() as ExecutionGraphQlServiceTester.Builder<*>
    return builder.configureExecutionInput { _, executionInputBuilder ->
        executionInputBuilder.graphQLContext(
            mapOf(AuthGraphQlContext.AUTHORIZATION to "Bearer access-token"),
        ).build()
    }.build()
}

internal fun graphQlProfileDetail() = ProfileDetail(
    profile = Profile.newInstance(workspaceId = 1L).copy(
        id = 10L,
        name = "잡도리",
        phone = "010-1111-2222",
        email = "rlajae14@gmail.com",
        coreCompetency = "핵심역량 내용",
    ),
    sections = ProfileSections(
        educations = listOf(
            Education(
                school = "잡도리대학교",
                major = "경영학과",
                degree = Degree.BACHELOR,
                status = EducationStatus.EXPECTED_GRADUATION,
                period = Period(
                    startAt = LocalDate.of(2020, 3, 1),
                    endAt = LocalDate.of(2026, 2, 28),
                ),
            ),
        ),
        careers = emptyList(),
        languageTests = emptyList(),
        awards = emptyList(),
        certifications = emptyList(),
        skills = listOf(
            ProfileSkill(name = "GA4", level = SkillLevel.HIGH),
        ),
    ),
)
