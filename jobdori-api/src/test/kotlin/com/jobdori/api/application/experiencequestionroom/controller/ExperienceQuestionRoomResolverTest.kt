package com.jobdori.api.application.experiencequestionroom.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.experiencequestionroom.dto.response.ExperienceQuestionRoomCardResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.experiencequestionroom.GetExperienceQuestionRoomsService
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomSourceType
import com.jobdori.core.domain.workspace.Workspace
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity

@GraphQLTest(ExperienceQuestionRoomResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class ExperienceQuestionRoomResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean private val accessTokenService: AccessTokenService,
    @MockkBean private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    @MockkBean private val getExperienceQuestionRoomsService: GetExperienceQuestionRoomsService,
) : StringSpec({

    "JD 경험 질문 대화방 카드 목록을 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { workspaceAccessValidationService.validateAccessible("ws-1", 1L) } returns
            Workspace(id = 10L, publicId = "ws-1", ownerUserId = 1L)
        every { getExperienceQuestionRoomsService.get(10L, "jd-pub-1") } returns listOf(
            ExperienceQuestionRoomCard("card-1", "장애 대응 체계를 직접 구축한 경험이 있나요?", ExperienceQuestionRoomSourceType.RESPONSIBILITY, "서비스 장애 대응"),
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  experienceQuestionRooms(workspaceId: "ws-1", jdId: "jd-pub-1") {
                    questionRoomId
                    question
                    sourceType
                    sourceText
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("experienceQuestionRooms[0]").entity<ExperienceQuestionRoomCardResponse>().satisfies {
                it shouldBe ExperienceQuestionRoomCardResponse(
                    "card-1", "장애 대응 체계를 직접 구축한 경험이 있나요?", ExperienceQuestionRoomSourceType.RESPONSIBILITY, "서비스 장애 대응",
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
