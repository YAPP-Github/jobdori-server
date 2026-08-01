package com.jobdori.api.application.credit.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.credit.CreditService
import com.jobdori.core.domain.credit.CreditBalance
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity
import java.time.LocalDate

@GraphQLTest(CreditQueryResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class CreditQueryResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val creditService: CreditService,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
    }

    "오늘 남은 크레딧과 하루 총 크레딧을 조회한다" {
        every { creditService.getBalance(1L) } returns creditBalance(remaining = 45)

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  credit {
                    remaining
                    total
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("credit.remaining").entity<Int>().isEqualTo(45)
            .path("credit.total").entity<Int>().isEqualTo(50)
    }

})

private fun creditBalance(remaining: Int) = CreditBalance(
    id = 1L,
    userId = 1L,
    remaining = remaining,
    lastResetDate = LocalDate.of(2026, 7, 30),
)

private fun authenticatedTester(graphQlTester: GraphQlTester): GraphQlTester {
    val builder = graphQlTester.mutate() as ExecutionGraphQlServiceTester.Builder<*>
    return builder.configureExecutionInput { _, executionInputBuilder ->
        executionInputBuilder.graphQLContext(
            mapOf(AuthGraphQlContext.AUTHORIZATION to "Bearer access-token"),
        ).build()
    }.build()
}
