package com.jobdori.api.application.user.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.user.dto.response.UserResponse
import com.jobdori.api.application.user.service.UserService
import com.jobdori.api.application.workspace.dto.response.WorkspaceResponse
import com.jobdori.api.application.workspace.service.WorkspaceService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity

@GraphQLTest(UserQueryResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class UserQueryResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val userService: UserService,
    @MockkBean
    private val workspaceService: WorkspaceService,
) : StringSpec({

    "인증된 사용자 정보를 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userService.getMe(1L) } returns UserResponse(
            id = 1L,
            userId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            email = "hong@example.com",
            name = "홍길동",
            profileImageUrl = "https://lh3.googleusercontent.com/profile",
        )
        every { workspaceService.getWorkspaces(1L) } returns listOf(
            WorkspaceResponse(workspaceId = "8f13f49e-132a-47b7-b704-d7eec18fd44b"),
        )

        authenticatedTester(graphQlTester)
            .documentName("me")
            .execute()
            .path("me.userId").entity<String>().isEqualTo("3f5c9d79-2255-4b76-bd31-013cd01d49d6")
            .path("me.email").entity<String>().isEqualTo("hong@example.com")
            .path("me.name").entity<String>().isEqualTo("홍길동")
            .path("me.profileImageUrl").entity<String>().isEqualTo("https://lh3.googleusercontent.com/profile")
            .path("me.workspaces[0].workspaceId").entity<String>()
            .isEqualTo("8f13f49e-132a-47b7-b704-d7eec18fd44b")

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) { userService.getMe(1L) }
        verify(exactly = 1) { workspaceService.getWorkspaces(1L) }
    }

    "워크스페이스 필드를 요청하지 않으면 워크스페이스를 조회하지 않는다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userService.getMe(1L) } returns UserResponse(
            id = 1L,
            userId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            email = "hong@example.com",
            name = "홍길동",
            profileImageUrl = "https://lh3.googleusercontent.com/profile",
        )

        authenticatedTester(graphQlTester)
            .documentName("me-without-workspaces")
            .execute()
            .path("me.userId").entity<String>().isEqualTo("3f5c9d79-2255-4b76-bd31-013cd01d49d6")
            .path("me.email").entity<String>().isEqualTo("hong@example.com")
            .path("me.name").entity<String>().isEqualTo("홍길동")
            .path("me.profileImageUrl").entity<String>().isEqualTo("https://lh3.googleusercontent.com/profile")

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) { userService.getMe(1L) }
        verify(exactly = 0) { workspaceService.getWorkspaces(any()) }
    }

    "인증 토큰이 없으면 사용자 정보를 조회할 수 없다" {
        graphQlTester.documentName("me")
            .execute()
            .errors()
            .satisfy { errors ->
                errors shouldHaveSize 1
                errors.first().extensions shouldContain ("code" to "invalid_auth_token")
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
