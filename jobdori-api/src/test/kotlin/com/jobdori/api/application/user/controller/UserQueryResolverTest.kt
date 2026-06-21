package com.jobdori.api.application.user.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.service.UserReader
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
    private val userReader: UserReader,
    @MockkBean
    private val accessTokenService: AccessTokenService,
) : StringSpec({

    "인증된 사용자 정보를 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userReader.getUser(1L) } returns User(
            id = 1L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )

        authenticatedTester(graphQlTester)
            .documentName("my-user")
            .execute()
            .path("myUser.userId").entity<String>().isEqualTo("3f5c9d79-2255-4b76-bd31-013cd01d49d6")

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) { userReader.getUser(1L) }
    }

    "인증 토큰이 없으면 사용자 정보를 조회할 수 없다" {
        graphQlTester.documentName("my-user")
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
