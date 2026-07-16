package com.jobdori.api.application.experience.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.experience.dto.request.CreateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.CreateExperienceRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceRequest
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.experience.service.ExperienceProjectService
import com.jobdori.api.application.experience.service.ExperienceService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.common.model.Period
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.experience.ExperienceContentsPolishService
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.StarExperienceContents
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity
import java.time.LocalDate

@GraphQLTest(ExperienceMutationResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class ExperienceMutationResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val experienceService: ExperienceService,
    @MockkBean
    private val experienceProjectService: ExperienceProjectService,
    @MockkBean
    private val experienceContentsPolishService: ExperienceContentsPolishService,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
    }

    "경험을 생성한다" {
        every {
            experienceService.createExperience(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 3L,
                request = any<CreateExperienceRequest>(),
            )
        } returns ExperienceResponse.from(
            experience = graphQlExperience(
                id = 100L,
                projectId = 3L,
                tags = listOf("브랜드런칭", "퍼포먼스마케팅"),
                title = "런칭 캠페인 메시지 A/B 테스트",
                contents = ExperienceContents.star("s", "t", "a", "r"),
            ),
            project = ExperienceProjectResponse.from(graphQlProject(3L)),
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  createExperience(
                    workspaceId: "workspace-id",
                    request: {
                      projectId: 3,
                      tags: ["브랜드런칭", "퍼포먼스마케팅"],
                      title: "런칭 캠페인 메시지 A/B 테스트",
                      contents: {
                        type: STAR,
                        star: {
                          situation: "s",
                          task: "t",
                          action: "a",
                          result: "r"
                        }
                      }
                    }
                  ) {
                    experienceId
                    project {
                      projectId
                    }
                    title
                    contents {
                      type
                      star {
                        situation
                      }
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("createExperience.experienceId").entity<String>().isEqualTo("100")
            .path("createExperience.project.projectId").entity<String>().isEqualTo("3")
            .path("createExperience.title").entity<String>().isEqualTo("런칭 캠페인 메시지 A/B 테스트")
            .path("createExperience.contents.type").entity<String>().isEqualTo("STAR")
            .path("createExperience.contents.star.situation").entity<String>().isEqualTo("s")

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) {
            experienceService.createExperience(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 3L,
                request = any<CreateExperienceRequest>(),
            )
        }
    }

    "경험을 수정한다" {
        every {
            experienceService.modifyExperience(
                userId = 1L,
                workspaceId = "workspace-id",
                experienceId = 100L,
                request = any<UpdateExperienceRequest>(),
            )
        } returns ExperienceResponse.from(
            experience = graphQlExperience(
                id = 100L,
                title = "프로젝트 회고",
                contents = ExperienceContents.free("회고 내용"),
            ),
            project = null,
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  updateExperience(
                    workspaceId: "workspace-id",
                    experienceId: 100,
                    request: {
                      projectId: 1,
                      tags: ["회고"],
                      title: "프로젝트 회고",
                      contents: {
                        type: FREE,
                        free: {
                          content: "회고 내용"
                        }
                      }
                    }
                  ) {
                    experienceId
                    title
                    contents {
                      type
                      free {
                        content
                      }
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("updateExperience.experienceId").entity<String>().isEqualTo("100")
            .path("updateExperience.title").entity<String>().isEqualTo("프로젝트 회고")
            .path("updateExperience.contents.type").entity<String>().isEqualTo("FREE")
            .path("updateExperience.contents.free.content").entity<String>().isEqualTo("회고 내용")

        verify(exactly = 1) {
            experienceService.modifyExperience(
                userId = 1L,
                workspaceId = "workspace-id",
                experienceId = 100L,
                request = any<UpdateExperienceRequest>(),
            )
        }
    }

    "경험을 삭제한다" {
        every {
            experienceService.removeExperience(
                userId = 1L,
                workspaceId = "workspace-id",
                experienceId = 100L,
            )
        } returns Unit

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  deleteExperience(workspaceId: "workspace-id", experienceId: 100)
                }
                """.trimIndent(),
            )
            .execute()
            .path("deleteExperience").entity<Boolean>().isEqualTo(true)

        verify(exactly = 1) {
            experienceService.removeExperience(
                userId = 1L,
                workspaceId = "workspace-id",
                experienceId = 100L,
            )
        }
    }

    "Free Style 경험 내용을 STAR 형식으로 다듬는다" {
        every {
            experienceContentsPolishService.polishFreeStyleToStar(
                content = "런칭 캠페인에서 메시지 A/B 테스트를 수행해 전환율을 개선했다.",
            )
        } returns StarExperienceContents(
            situation = "신규 서비스 런칭 캠페인에서 메시지별 전환 성과를 검증해야 했다.",
            task = "캠페인 메시지 A/B 테스트를 설계하고 성과가 높은 메시지를 찾아야 했다.",
            action = "메시지 안을 나누어 테스트를 운영하고 전환율 데이터를 비교했다.",
            result = "성과가 높은 메시지를 캠페인에 반영해 전환율을 개선했다.",
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  polishExperienceContents(
                    request: {
                      content: "런칭 캠페인에서 메시지 A/B 테스트를 수행해 전환율을 개선했다."
                    }
                  ) {
                    situation
                    task
                    action
                    result
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("polishExperienceContents.situation").entity<String>()
            .isEqualTo("신규 서비스 런칭 캠페인에서 메시지별 전환 성과를 검증해야 했다.")
            .path("polishExperienceContents.task").entity<String>()
            .isEqualTo("캠페인 메시지 A/B 테스트를 설계하고 성과가 높은 메시지를 찾아야 했다.")
            .path("polishExperienceContents.action").entity<String>()
            .isEqualTo("메시지 안을 나누어 테스트를 운영하고 전환율 데이터를 비교했다.")
            .path("polishExperienceContents.result").entity<String>()
            .isEqualTo("성과가 높은 메시지를 캠페인에 반영해 전환율을 개선했다.")

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) {
            experienceContentsPolishService.polishFreeStyleToStar(
                content = "런칭 캠페인에서 메시지 A/B 테스트를 수행해 전환율을 개선했다.",
            )
        }
    }

    "경험 프로젝트를 생성한다" {
        every {
            experienceProjectService.createProject(
                userId = 1L,
                workspaceId = "workspace-id",
                request = any<CreateExperienceProjectRequest>(),
            )
        } returns ExperienceProjectResponse.from(graphQlProject(100L))

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  createExperienceProject(
                    workspaceId: "workspace-id",
                    input: {
                      name: "신규 브랜드 런칭 캠페인",
                      summary: "신규 서비스의 초기 인지도 확보 캠페인",
                      period: {
                        startAt: "2025-01-01",
                        endAt: "2025-04-30"
                      },
                      role: "Growth Marketer"
                    }
                  ) {
                    projectId
                    name
                    period {
                      startAt
                      endAt
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("createExperienceProject.projectId").entity<String>().isEqualTo("100")
            .path("createExperienceProject.name").entity<String>().isEqualTo("신규 브랜드 런칭 캠페인")
            .path("createExperienceProject.period.startAt").entity<String>().isEqualTo("2025-01-01")

        verify(exactly = 1) {
            experienceProjectService.createProject(
                userId = 1L,
                workspaceId = "workspace-id",
                request = any<CreateExperienceProjectRequest>(),
            )
        }
    }

    "경험 프로젝트를 수정한다" {
        every {
            experienceProjectService.modifyProject(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 100L,
                request = any<UpdateExperienceProjectRequest>(),
            )
        } returns ExperienceProjectResponse.from(graphQlProject(100L, role = "Brand Growth Lead"))

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  updateExperienceProject(
                    workspaceId: "workspace-id",
                    projectId: 100,
                    request: {
                      name: "신규 브랜드 런칭 캠페인",
                      summary: "브랜드 런칭 캠페인을 진행한 프로젝트",
                      role: "Brand Growth Lead"
                    }
                  ) {
                    projectId
                    role
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("updateExperienceProject.projectId").entity<String>().isEqualTo("100")
            .path("updateExperienceProject.role").entity<String>().isEqualTo("Brand Growth Lead")

        verify(exactly = 1) {
            experienceProjectService.modifyProject(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 100L,
                request = any<UpdateExperienceProjectRequest>(),
            )
        }
    }

    "경험 프로젝트를 삭제한다" {
        every {
            experienceProjectService.removeProject(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 100L,
            )
        } returns Unit

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  deleteExperienceProject(workspaceId: "workspace-id", projectId: 100)
                }
                """.trimIndent(),
            )
            .execute()
            .path("deleteExperienceProject").entity<Boolean>().isEqualTo(true)

        verify(exactly = 1) {
            experienceProjectService.removeProject(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 100L,
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

private fun graphQlExperience(
    id: Long,
    projectId: Long = 3L,
    tags: List<String> = emptyList(),
    title: String = "경험 $id",
    contents: ExperienceContents,
) = Experience(
    id = id,
    workspaceId = 1L,
    projectId = projectId,
    tags = tags,
    title = title,
    contents = contents,
    displayOrder = 0.0,
    status = ExperienceStatus.ACTIVE,
)

private fun graphQlProject(
    id: Long,
    role: String? = "Growth Marketer",
) = ExperienceProject(
    id = id,
    workspaceId = 1L,
    name = "신규 브랜드 런칭 캠페인",
    summary = "신규 서비스의 초기 인지도 확보 캠페인",
    period = Period(
        startAt = LocalDate.of(2025, 1, 1),
        endAt = LocalDate.of(2025, 4, 30),
    ),
    role = role,
    displayOrder = 0.0,
    status = ExperienceProjectStatus.ACTIVE,
)
