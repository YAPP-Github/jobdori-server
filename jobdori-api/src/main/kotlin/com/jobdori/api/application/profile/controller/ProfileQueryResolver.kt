package com.jobdori.api.application.profile.controller

import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.profile.service.ProfileService
import com.jobdori.api.support.auth.UserId
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ProfileQueryResolver(
    private val profileService: ProfileService,
) {

    @QueryMapping
    fun profile(
        @UserId userId: Long,
        @Argument workspaceId: String,
    ): ProfileResponse = profileService.getProfile(
        userId = userId,
        workspaceId = workspaceId,
    )

}
