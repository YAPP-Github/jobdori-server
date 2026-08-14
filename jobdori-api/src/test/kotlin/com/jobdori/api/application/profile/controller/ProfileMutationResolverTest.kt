package com.jobdori.api.application.profile.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.profile.dto.request.PolishProfileTextRequest
import com.jobdori.api.application.profile.dto.request.UpdateProfileRequest
import com.jobdori.api.application.profile.dto.response.GenerateCoreCompetencyResponse
import com.jobdori.api.application.profile.dto.response.PolishedProfileTextResponse
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.profile.service.ProfileService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.profile.PolishStructure
import com.jobdori.core.application.profile.ProfilePolishKind
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.execution.ErrorType
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

    "핵심역량을 AI로 생성한다 (jdId를 주면 표시용 지원 전략 함께 반환)" {
        every {
            profileService.generateCoreCompetency(
                userId = 1L,
                workspaceId = "workspace-id",
                resumeId = 100L,
                jdId = "jd-pub-1",
            )
        } returns GenerateCoreCompetencyResponse(
            coreCompetency = "콘텐츠 기획과 데이터 기반 개선에 강점이 있는 마케터입니다.",
            strategy = "데이터 기반 개선 경험을 강조해서 지원하는 게 좋겠어요.",
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  generateCoreCompetency(workspaceId: "workspace-id", resumeId: 100, jdId: "jd-pub-1") {
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
            profileService.generateCoreCompetency(
                userId = 1L,
                workspaceId = "workspace-id",
                resumeId = 100L,
                jdId = "jd-pub-1",
            )
        }
    }

    "프로필 텍스트를 AI로 다듬는다" {
        every {
            profileService.polishProfileText(
                userId = 1L,
                workspaceId = null,
                request = PolishProfileTextRequest(
                    description = "런칭 캠페인에서 A/B 테스트를 수행해서 전환율을 많이 개선했음",
                    kind = ProfilePolishKind.CAREER_DESCRIPTION,
                ),
            )
        } returns PolishedProfileTextResponse(
            title = null,
            description = "런칭 캠페인에서 메시지 A/B 테스트를 설계해 전환율을 개선했습니다.",
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  polishProfileText(
                    request: {
                      description: "런칭 캠페인에서 A/B 테스트를 수행해서 전환율을 많이 개선했음",
                      kind: CAREER_DESCRIPTION
                    }
                  ) {
                    title
                    description
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("polishProfileText.title").valueIsNull()
            .path("polishProfileText.description").entity<String>()
            .isEqualTo("런칭 캠페인에서 메시지 A/B 테스트를 설계해 전환율을 개선했습니다.")

        verify(exactly = 1) {
            profileService.polishProfileText(
                userId = 1L,
                workspaceId = null,
                request = PolishProfileTextRequest(
                    description = "런칭 캠페인에서 A/B 테스트를 수행해서 전환율을 많이 개선했음",
                    kind = ProfilePolishKind.CAREER_DESCRIPTION,
                ),
            )
        }
    }

    "작성 구조/직접 지침/JD를 주면 옵션을 반영해 다듬는다" {
        val request = PolishProfileTextRequest(
            description = "경험이 이미 채워져있는 상태입니다",
            kind = ProfilePolishKind.CAREER_DESCRIPTION,
            structure = PolishStructure.BULLET,
            instruction = "정중한 어투로, 전환율 수치를 강조",
            jdId = "jd-pub-1",
        )
        every {
            profileService.polishProfileText(userId = 1L, workspaceId = "workspace-id", request = request)
        } returns PolishedProfileTextResponse(
            title = null,
            description = "- AI가 수정 지침에 따라 수정해준 완성본",
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  polishProfileText(
                    workspaceId: "workspace-id",
                    request: {
                      description: "경험이 이미 채워져있는 상태입니다",
                      kind: CAREER_DESCRIPTION,
                      structure: BULLET,
                      instruction: "정중한 어투로, 전환율 수치를 강조",
                      jdId: "jd-pub-1"
                    }
                  ) {
                    title
                    description
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("polishProfileText.title").valueIsNull()
            .path("polishProfileText.description").entity<String>()
            .isEqualTo("- AI가 수정 지침에 따라 수정해준 완성본")

        verify(exactly = 1) {
            profileService.polishProfileText(userId = 1L, workspaceId = "workspace-id", request = request)
        }
    }

    "경험 첨삭에서 경험명이 없으면 잘못된 요청으로 응답한다" {
        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  polishProfileText(
                    request: {
                      description: "경험이 이미 채워져있는 상태입니다",
                      kind: EXPERIENCE
                    }
                  ) {
                    title
                    description
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .errors()
            .satisfy { errors ->
                errors shouldHaveSize 1
                val error = errors.first()
                error.errorType shouldBe ErrorType.BAD_REQUEST
                error.message shouldBe "경험을 첨삭하려면 제목을 입력해 주세요."
                error.extensions shouldContain (
                    "code" to CommonErrorCode.E400_INVALID_ARGUMENTS.code
                )
            }

        verify(exactly = 0) {
            profileService.polishProfileText(
                userId = any(),
                workspaceId = any(),
                request = any(),
            )
        }
    }

    "경험명과 경험 내용을 한 번에 AI로 다듬는다" {
        val request = PolishProfileTextRequest(
            title = "콘텐츠 마케팅 캠페인 운영",
            description = "경험이 이미 채워져있는 상태입니다",
            kind = ProfilePolishKind.EXPERIENCE,
            structure = PolishStructure.BULLET,
            instruction = "경험명과 경험 내용에서 전환율 개선 성과를 강조",
            jdId = "jd-pub-1",
        )
        every {
            profileService.polishProfileText(userId = 1L, workspaceId = "workspace-id", request = request)
        } returns PolishedProfileTextResponse(
            title = "전환율을 개선한 콘텐츠 마케팅 캠페인",
            description = "- 메시지 A/B 테스트를 수행해 전환율을 개선했습니다.",
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  polishProfileText(
                    workspaceId: "workspace-id",
                    request: {
                      title: "콘텐츠 마케팅 캠페인 운영",
                      description: "경험이 이미 채워져있는 상태입니다",
                      kind: EXPERIENCE,
                      structure: BULLET,
                      instruction: "경험명과 경험 내용에서 전환율 개선 성과를 강조",
                      jdId: "jd-pub-1"
                    }
                  ) {
                    title
                    description
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("polishProfileText.title").entity<String>()
            .isEqualTo("전환율을 개선한 콘텐츠 마케팅 캠페인")
            .path("polishProfileText.description").entity<String>()
            .isEqualTo("- 메시지 A/B 테스트를 수행해 전환율을 개선했습니다.")

        verify(exactly = 1) {
            profileService.polishProfileText(userId = 1L, workspaceId = "workspace-id", request = request)
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
