package com.jobdori.api.application.experience.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.experience.dto.response.ExperienceListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.experience.service.ExperienceProjectService
import com.jobdori.api.application.experience.service.ExperienceService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.ExperienceStatus
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity
import java.math.BigDecimal

@GraphQLTest(ExperienceQueryResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class ExperienceQueryResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val experienceService: ExperienceService,
    @MockkBean
    private val experienceProjectService: ExperienceProjectService,
) : StringSpec({

    "경험 목록은 GraphQL에서 cursor와 size를 받고 Resolver 내부에서는 CursorRequest로 처리한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every {
            experienceService.getExperiences(
                1L,
                "workspace-id",
                null,
                null,
                2,
                false
            )
        } returns ExperienceListResponse(
            experiences = listOf(
                ExperienceResponse.from(
                    experience = graphQlExperience(5L, ExperienceContents.star("s", "t", "a", "r")),
                    project = null,
                ),
                ExperienceResponse.from(
                    experience = graphQlExperience(4L, ExperienceContents.free("free")),
                    project = null,
                ),
            ),
            cursor = CursorResponse(nextCursor = "4"),
        )

        authenticatedTester(graphQlTester)
            .documentName("experiences")
            .execute()
            .path("experiences.experiences[0].experienceId").entity<String>().isEqualTo("5")
            .path("experiences.experiences[1].experienceId").entity<String>().isEqualTo("4")
            .path("experiences.cursor.nextCursor").entity<String>().isEqualTo("4")
            .path("experiences.cursor.hasNext").entity<Boolean>().isEqualTo(true)

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) { experienceService.getExperiences(1L, "workspace-id", null, null, 2, false) }
        verify(exactly = 0) { experienceProjectService.getProjects(any(), any(), any(), any(), any()) }
    }

    "경험 목록에서 project를 요청하면 프로젝트를 한 번에 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        val project = ExperienceProject(
            id = 3L,
            workspaceId = 1L,
            name = "신규 브랜드 런칭 캠페인",
            summary = "신규 서비스의 초기 인지도 확보 캠페인",
            period = null,
            role = "Growth Marketer",
            displayOrder = BigDecimal.ZERO,
            status = ExperienceProjectStatus.ACTIVE,
        )
        every {
            experienceService.getExperiences(
                1L,
                "workspace-id",
                null,
                null,
                2,
                true
            )
        } returns ExperienceListResponse(
            experiences = listOf(
                ExperienceResponse.from(
                    experience = graphQlExperience(5L, ExperienceContents.star("s", "t", "a", "r")),
                    project = ExperienceProjectResponse.from(project),
                ),
                ExperienceResponse.from(
                    experience = graphQlExperience(4L, ExperienceContents.free("free")),
                    project = ExperienceProjectResponse.from(project),
                ),
            ),
            cursor = CursorResponse(nextCursor = "4"),
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                {
                  experiences(workspaceId: "workspace-id", cursor: null, size: 2) {
                    experiences {
                      experienceId
                      project {
                        projectId
                        name
                      }
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("experiences.experiences[0].project.projectId").entity<String>().isEqualTo("3")
            .path("experiences.experiences[0].project.name").entity<String>().isEqualTo("신규 브랜드 런칭 캠페인")
            .path("experiences.experiences[1].project.projectId").entity<String>().isEqualTo("3")

        verify(exactly = 1) { experienceService.getExperiences(1L, "workspace-id", null, null, 2, true) }
        verify(exactly = 0) { experienceProjectService.getProjects(any(), any(), any(), any(), any()) }
    }

    "경험 프로젝트 단건을 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        val project = ExperienceProject(
            id = 3L,
            workspaceId = 1L,
            name = "프로젝트",
            summary = "요약",
            period = null,
            role = "백엔드",
            displayOrder = BigDecimal.ZERO,
            status = ExperienceProjectStatus.ACTIVE,
        )
        every {
            experienceProjectService.getProject(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 3L,
                includeExperienceCount = false,
            )
        } returns ExperienceProjectResponse.from(project)

        authenticatedTester(graphQlTester)
            .document(
                """
                {
                  experienceProject(workspaceId: "workspace-id", projectId: 3) {
                    projectId
                    name
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("experienceProject.projectId").entity<String>().isEqualTo("3")
            .path("experienceProject.name").entity<String>().isEqualTo("프로젝트")

        verify(exactly = 1) {
            experienceProjectService.getProject(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 3L,
                includeExperienceCount = false,
            )
        }
    }

    "경험 프로젝트 목록에서 experienceCount를 요청하면 카운트 포함 옵션을 넘긴다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        val project = ExperienceProject(
            id = 3L,
            workspaceId = 1L,
            name = "프로젝트",
            summary = "요약",
            period = null,
            role = "백엔드",
            displayOrder = BigDecimal.ZERO,
            status = ExperienceProjectStatus.ACTIVE,
        )
        every {
            experienceProjectService.getProjects(
                userId = 1L,
                workspaceId = "workspace-id",
                cursor = null,
                size = 2,
                includeExperienceCount = true,
            )
        } returns com.jobdori.api.application.experience.dto.response.ExperienceProjectListResponse(
            projects = listOf(ExperienceProjectResponse.from(project, experienceCount = 7L)),
            cursor = CursorResponse(nextCursor = null),
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                {
                  experienceProjects(workspaceId: "workspace-id", cursor: null, size: 2) {
                    projects {
                      projectId
                      experienceCount
                    }
                    cursor {
                      hasNext
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("experienceProjects.projects[0].projectId").entity<String>().isEqualTo("3")
            .path("experienceProjects.projects[0].experienceCount").entity<Long>().isEqualTo(7L)

        verify(exactly = 1) {
            experienceProjectService.getProjects(
                userId = 1L,
                workspaceId = "workspace-id",
                cursor = null,
                size = 2,
                includeExperienceCount = true,
            )
        }
    }

    "검색어가 포함된 경험 목록을 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every {
            experienceService.searchExperiences(
                1L,
                "workspace-id",
                "kotlin",
                null,
                2,
                false,
            )
        } returns ExperienceListResponse(
            experiences = listOf(
                ExperienceResponse.from(
                    experience = graphQlExperience(
                        id = 5L,
                        contents = ExperienceContents.free("kotlin coroutine 개선"),
                        tags = emptyList(),
                    ),
                    project = null,
                ),
            ),
            cursor = CursorResponse(nextCursor = null),
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                {
                  searchExperiences(workspaceId: "workspace-id", keyword: "kotlin", cursor: null, size: 2) {
                    experiences {
                      experienceId
                      title
                    }
                    cursor {
                      nextCursor
                      hasNext
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("searchExperiences.experiences[0].experienceId").entity<String>().isEqualTo("5")
            .path("searchExperiences.experiences[0].title").entity<String>().isEqualTo("경험 5")
            .path("searchExperiences.cursor.hasNext").entity<Boolean>().isEqualTo(false)

        verify(exactly = 1) { experienceService.searchExperiences(1L, "workspace-id", "kotlin", null, 2, false) }
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
    contents: ExperienceContents,
    tags: List<String> = emptyList(),
) = Experience(
    id = id,
    workspaceId = 1L,
    projectId = 3L,
    tags = tags,
    title = "경험 $id",
    contents = contents,
    displayOrder = BigDecimal.ZERO,
    status = ExperienceStatus.ACTIVE,
)
