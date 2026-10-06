package com.jobdori.api.application.experiencequestionroom.controller

import com.jobdori.api.application.experiencequestionroom.dto.request.SendExperienceQuestionRoomMessageRequest
import com.jobdori.api.application.experiencequestionroom.dto.response.ExperienceQuestionRoomCardResponse
import com.jobdori.api.application.experiencequestionroom.dto.response.ExperienceQuestionRoomDetailResponse
import com.jobdori.api.application.experiencequestionroom.dto.response.ExperienceQuestionRoomMessageResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.experiencequestionroom.ExperienceQuestionRoomChatService
import com.jobdori.core.application.experiencequestionroom.GetExperienceQuestionRoomsService
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ExperienceQuestionRoomResolver(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val getExperienceQuestionRoomsService: GetExperienceQuestionRoomsService,
    private val experienceQuestionRoomChatService: ExperienceQuestionRoomChatService,
) {

    @QueryMapping
    fun experienceQuestionRooms(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument jdId: String,
    ): List<ExperienceQuestionRoomCardResponse> {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        return getExperienceQuestionRoomsService.get(workspace.id, jdId).map(ExperienceQuestionRoomCardResponse::from)
    }

    @QueryMapping
    fun experienceQuestionRoom(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument questionRoomId: String,
    ): ExperienceQuestionRoomDetailResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        return ExperienceQuestionRoomDetailResponse.from(experienceQuestionRoomChatService.getDetail(workspace.id, questionRoomId))
    }

    @MutationMapping
    fun sendExperienceQuestionRoomMessage(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument questionRoomId: String,
        @Valid @Argument request: SendExperienceQuestionRoomMessageRequest,
    ): ExperienceQuestionRoomMessageResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        val message = experienceQuestionRoomChatService.send(
            workspaceId = workspace.id,
            questionRoomId = questionRoomId,
            content = request.content,
            experienceIds = request.experienceIds,
        )
        return ExperienceQuestionRoomMessageResponse.from(message)
    }
}
