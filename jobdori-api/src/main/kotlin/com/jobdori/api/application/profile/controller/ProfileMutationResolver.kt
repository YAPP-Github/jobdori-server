package com.jobdori.api.application.profile.controller

import com.jobdori.api.application.profile.dto.request.PolishProfileTextRequest
import com.jobdori.api.application.profile.dto.request.UpdateProfileRequest
import com.jobdori.api.application.profile.dto.response.GenerateCoreCompetencyResponse
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.profile.service.ProfileService
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.profile.ProfileAiService
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class ProfileMutationResolver(
    private val profileService: ProfileService,
    private val profileAiService: ProfileAiService,
) {

    @MutationMapping
    fun updateProfile(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Valid @Argument request: UpdateProfileRequest,
    ): ProfileResponse = profileService.updateProfile(
        userId = userId,
        workspaceId = workspaceId,
        request = request,
    )

    @MutationMapping
    fun generateCoreCompetency(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument jdId: String?,
    ): GenerateCoreCompetencyResponse = profileService.generateCoreCompetency(
        userId = userId,
        workspaceId = workspaceId,
        jdId = jdId,
    )

    @MutationMapping
    fun polishProfileText(
        @UserId userId: Long,
        @Valid @Argument request: PolishProfileTextRequest,
    ): String = profileAiService.polish(
        text = request.text,
        kind = request.kind,
    )

}
