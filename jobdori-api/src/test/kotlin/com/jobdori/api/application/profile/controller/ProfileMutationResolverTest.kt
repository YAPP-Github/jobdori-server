package com.jobdori.api.application.profile.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.profile.dto.request.UpdateProfileRequest
import com.jobdori.api.application.profile.dto.response.GenerateCoreCompetencyResponse
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.profile.service.ProfileService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.profile.ProfileAiService
import com.jobdori.core.application.profile.ProfilePolishKind
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity

@GraphQLTest(ProfileMutationResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class ProfileMutationResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val profileService: ProfileService,
    @MockkBean
    private val profileAiService: ProfileAiService,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
    }

    "이력서 기본 정보 프로필을 수정한다" {
        every {
            profileService.updateProfile(
                userId = 1L,
                workspaceId = "workspace-id",
                request = any<UpdateProfileRequest>(),
            )
        } returns ProfileResponse.from(graphQlProfileDetail())

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  updateProfile(
                    workspaceId: "workspace-id",
                    request: {
                      name: "잡도리",
                      coreCompetency: "핵심역량 내용",
                      educations: [
                        {
                          school: "잡도리대학교",
                          major: "경영학과",
                          degree: BACHELOR,
                          status: EXPECTED_GRADUATION,
                          period: {
                            startAt: "2020-03-01",
                            endAt: "2026-02-28"
                          }
                        }
                      ],
                      skills: [
                        {
                          name: "GA4",
                          level: HIGH
                        }
                      ]
                    }
                  ) {
                    profileId
                    name
                    coreCompetency
                    educations {
                      school
                      degree
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
            .path("updateProfile.profileId").entity<String>().isEqualTo("10")
            .path("updateProfile.name").entity<String>().isEqualTo("잡도리")
            .path("updateProfile.coreCompetency").entity<String>().isEqualTo("핵심역량 내용")
            .path("updateProfile.educations[0].school").entity<String>().isEqualTo("잡도리대학교")
            .path("updateProfile.educations[0].degree").entity<String>().isEqualTo("BACHELOR")
            .path("updateProfile.skills[0].level").entity<String>().isEqualTo("HIGH")

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) {
            profileService.updateProfile(
                userId = 1L,
                workspaceId = "workspace-id",
                request = any<UpdateProfileRequest>(),
            )
        }
    }

    "핵심역량을 AI로 생성한다 (jdId를 주면 지원 전략 기준 생성과 전략 반환)" {
        every {
            profileService.generateCoreCompetency(userId = 1L, workspaceId = "workspace-id", jdId = "jd-pub-1")
        } returns GenerateCoreCompetencyResponse(
            coreCompetency = "콘텐츠 기획과 데이터 기반 개선에 강점이 있는 마케터입니다.",
            strategy = "데이터 기반 개선 경험을 강조해서 지원하는 게 좋겠어요.",
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  generateCoreCompetency(workspaceId: "workspace-id", jdId: "jd-pub-1") {
                    coreCompetency
                    strategy
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("generateCoreCompetency.coreCompetency").entity<String>()
            .isEqualTo("콘텐츠 기획과 데이터 기반 개선에 강점이 있는 마케터입니다.")
            .path("generateCoreCompetency.strategy").entity<String>()
            .isEqualTo("데이터 기반 개선 경험을 강조해서 지원하는 게 좋겠어요.")

        verify(exactly = 1) {
            profileService.generateCoreCompetency(userId = 1L, workspaceId = "workspace-id", jdId = "jd-pub-1")
        }
    }

    "프로필 텍스트를 AI로 다듬는다" {
        every {
            profileAiService.polish(
                text = "런칭 캠페인에서 A/B 테스트를 수행해서 전환율을 많이 개선했음",
                kind = ProfilePolishKind.CAREER_DESCRIPTION,
            )
        } returns "런칭 캠페인에서 메시지 A/B 테스트를 설계해 전환율을 개선했습니다."

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  polishProfileText(
                    request: {
                      text: "런칭 캠페인에서 A/B 테스트를 수행해서 전환율을 많이 개선했음",
                      kind: CAREER_DESCRIPTION
                    }
                  )
                }
                """.trimIndent(),
            )
            .execute()
            .path("polishProfileText").entity<String>()
            .isEqualTo("런칭 캠페인에서 메시지 A/B 테스트를 설계해 전환율을 개선했습니다.")

        verify(exactly = 1) {
            profileAiService.polish(
                text = "런칭 캠페인에서 A/B 테스트를 수행해서 전환율을 많이 개선했음",
                kind = ProfilePolishKind.CAREER_DESCRIPTION,
            )
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
