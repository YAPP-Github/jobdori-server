package com.jobdori.api.application.experiencequestionroom.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.experiencequestionroom.dto.response.ExperienceQuestionRoomCardResponse
import com.jobdori.api.application.experiencequestionroom.dto.response.ExperienceQuestionRoomDetailResponse
import com.jobdori.api.application.experiencequestionroom.dto.response.ExperienceQuestionRoomMessageResponse
import com.jobdori.api.application.resume.dto.response.ResumeSectionItemResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.experiencequestionroom.ExperienceQuestionRoomChatService
import com.jobdori.core.application.experiencequestionroom.GetExperienceQuestionRoomsService
import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomCardResult
import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomDetail
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessageRole
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomSourceType
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeSectionItem
import com.jobdori.core.domain.workspace.Workspace
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity
import java.time.LocalDateTime

@GraphQLTest(ExperienceQuestionRoomResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class ExperienceQuestionRoomResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean private val accessTokenService: AccessTokenService,
    @MockkBean private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    @MockkBean private val getExperienceQuestionRoomsService: GetExperienceQuestionRoomsService,
    @MockkBean private val experienceQuestionRoomChatService: ExperienceQuestionRoomChatService,
) : StringSpec({

    "JD 경험 질문 대화방 카드 목록을 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { workspaceAccessValidationService.validateAccessible("ws-1", 1L) } returns
            Workspace(id = 10L, publicId = "ws-1", ownerUserId = 1L)
        every { getExperienceQuestionRoomsService.get(10L, "jd-pub-1") } returns listOf(
            ExperienceQuestionRoomCardResult(
                ExperienceQuestionRoomCard("card-1", "장애 대응 체계를 직접 구축한 경험이 있나요?", ExperienceQuestionRoomSourceType.RESPONSIBILITY, "서비스 장애 대응"),
                applied = true,
            ),
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
                    applied
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("experienceQuestionRooms[0]").entity<ExperienceQuestionRoomCardResponse>().satisfies {
                it shouldBe ExperienceQuestionRoomCardResponse(
                    "card-1", "장애 대응 체계를 직접 구축한 경험이 있나요?", ExperienceQuestionRoomSourceType.RESPONSIBILITY, "서비스 장애 대응", true,
                )
            }
    }

    "질문 대화방 상세를 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { workspaceAccessValidationService.validateAccessible("ws-1", 1L) } returns
            Workspace(id = 10L, publicId = "ws-1", ownerUserId = 1L)
        val room = ExperienceQuestionRoomCard("room-1", "직접 개선한 경험이 있나요?", ExperienceQuestionRoomSourceType.RESPONSIBILITY, "서비스 개선")
        val user = ExperienceQuestionRoomMessage.user("room-1", "정리해 주세요", listOf(8L)).copy(messageId = 7L, createdAt = createdAt)
        every { experienceQuestionRoomChatService.getDetail(10L, "room-1") } returns ExperienceQuestionRoomDetail(ExperienceQuestionRoomCardResult(room, applied = false), listOf(user))

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  experienceQuestionRoom(workspaceId: "ws-1", questionRoomId: "room-1") {
                    room { questionRoomId question sourceType sourceText applied }
                    messages { messageId role content experienceIds block { title bullets } feedback { fit improvement } createdAt }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("experienceQuestionRoom").entity<ExperienceQuestionRoomDetailResponse>().satisfies {
                it.room.questionRoomId shouldBe "room-1"
                it.messages.single().role shouldBe ExperienceQuestionRoomMessageRole.USER
                it.messages.single().experienceIds shouldBe listOf(8L)
            }
    }

    "대화방 메시지를 보내고 AI 블록을 반환한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { workspaceAccessValidationService.validateAccessible("ws-1", 1L) } returns
            Workspace(id = 10L, publicId = "ws-1", ownerUserId = 1L)
        val ai = ExperienceQuestionRoomMessage.ai(
            questionRoomId = "room-1",
            blockTitle = "운영 개선",
            bullets = listOf("모니터링을 개선했다.", "장애 대응 시간을 줄였다."),
            fit = "요구에 부합해요.",
            improvement = "수치를 보강해 주세요.",
        ).copy(messageId = 9L, createdAt = createdAt)
        every { experienceQuestionRoomChatService.send(10L, "room-1", "정리해 주세요", listOf(8L)) } returns ai

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  sendExperienceQuestionRoomMessage(
                    workspaceId: "ws-1", questionRoomId: "room-1", request: { content: "정리해 주세요", experienceIds: ["8"] }
                  ) {
                    messageId role block { title bullets } feedback { fit improvement } createdAt
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("sendExperienceQuestionRoomMessage").entity<ExperienceQuestionRoomMessageResponse>().satisfies {
                it.messageId shouldBe 9L
                it.block?.title shouldBe "운영 개선"
            }
    }

    "AI 블록을 이력서에 적용한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { workspaceAccessValidationService.validateAccessible("ws-1", 1L) } returns
            Workspace(id = 10L, publicId = "ws-1", ownerUserId = 1L)
        val item = ResumeSectionItem(
            id = 12L,
            sectionId = 3L,
            payload = ResumeExperiencePayload("운영 개선", null, null, "- 모니터링을 개선했다.\n- 장애 대응 시간을 줄였다."),
            displayOrder = 2.0,
            visible = true,
            createdAt = createdAt,
            updatedAt = createdAt,
        )
        every { experienceQuestionRoomChatService.apply(10L, "room-1", 9L, 4L) } returns item

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  applyExperienceQuestionRoomBlock(workspaceId: "ws-1", questionRoomId: "room-1", messageId: "9", resumeId: "4") {
                    itemId displayOrder visible payload { experience { name contents } } createdAt
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("applyExperienceQuestionRoomBlock").entity<ResumeSectionItemResponse>().satisfies {
                it.itemId shouldBe 12L
            }
    }
})

private val createdAt = LocalDateTime.parse("2026-01-01T10:00:00")

private fun authenticatedTester(graphQlTester: GraphQlTester): GraphQlTester {
    val builder = graphQlTester.mutate() as ExecutionGraphQlServiceTester.Builder<*>
    return builder.configureExecutionInput { _, executionInputBuilder ->
        executionInputBuilder.graphQLContext(
            mapOf(AuthGraphQlContext.AUTHORIZATION to "Bearer access-token"),
        ).build()
    }.build()
}
