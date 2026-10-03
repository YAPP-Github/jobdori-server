package com.jobdori.api.application.experiencequestionroom.controller

import com.jobdori.api.application.experiencequestionroom.dto.response.ExperienceQuestionRoomCardResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.experiencequestionroom.GetExperienceQuestionRoomsService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ExperienceQuestionRoomResolver(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val getExperienceQuestionRoomsService: GetExperienceQuestionRoomsService,
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
}
