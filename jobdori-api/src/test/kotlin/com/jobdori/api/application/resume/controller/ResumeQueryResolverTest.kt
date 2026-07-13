package com.jobdori.api.application.resume.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.api.application.resume.dto.response.ResumeStatusCountResponse
import com.jobdori.api.application.resume.service.ResumeService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity

@GraphQLTest(ResumeQueryResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class ResumeQueryResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val resumeService: ResumeService,
) : StringSpec({

    "이력서 수를 상태별로 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { resumeService.countResumes(1L, "workspace-id") } returns listOf(
            ResumeStatusCountResponse(status = ResumeStatusType.COMPLETED, count = 2L),
            ResumeStatusCountResponse(status = ResumeStatusType.DRAFT, count = 1L),
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                {
                  resumeCounts(workspaceId: "workspace-id") {
                    status
                    count
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("resumeCounts[0].status").entity<String>().isEqualTo("COMPLETED")
            .path("resumeCounts[0].count").entity<Int>().isEqualTo(2)
            .path("resumeCounts[1].status").entity<String>().isEqualTo("DRAFT")
            .path("resumeCounts[1].count").entity<Int>().isEqualTo(1)

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) { resumeService.countResumes(1L, "workspace-id") }
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
