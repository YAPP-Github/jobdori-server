package com.jobdori.api.application.profile.controller

import com.jobdori.api.application.profile.dto.request.SuggestKeywordsRequest
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.profile.service.ProfileService
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.domain.keyword.service.KeywordReader
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.Arguments
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ProfileQueryResolver(
    private val profileService: ProfileService,
    private val keywordReader: KeywordReader,
) {

    @QueryMapping
    fun profile(
        @UserId userId: Long,
        @Argument workspaceId: String,
    ): ProfileResponse = profileService.getProfile(
        userId = userId,
        workspaceId = workspaceId,
    )

    @QueryMapping
    fun suggestKeywords(
        @UserId userId: Long,
        @Valid @Arguments request: SuggestKeywordsRequest,
    ): List<String> = keywordReader.suggest(
        type = request.type,
        keyword = request.keyword,
        size = request.size,
    )

}
