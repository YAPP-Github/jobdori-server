package com.jobdori.api.application.jdrecommendation.controller

import com.jobdori.api.application.jdrecommendation.dto.response.JdRecommendationListResponse
import com.jobdori.api.application.jdrecommendation.dto.request.ListJdRecommendationRequest
import com.jobdori.api.application.jdrecommendation.dto.response.JdRecommendationTagResponse
import com.jobdori.core.application.jdrecommendation.GetJdRecommendationService
import com.jobdori.core.domain.jdrecommendation.JdRecommendationTag
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Arguments
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class JdRecommendationQueryResolver(
    private val getJdRecommendationService: GetJdRecommendationService,
) {

    @QueryMapping
    fun jdRecommendationTags(): List<JdRecommendationTagResponse> =
        JdRecommendationTag.entries.map(JdRecommendationTagResponse::from)

    @QueryMapping
    fun jdRecommendations(
        @Argument tags: List<JdRecommendationTag>,
        @Valid @Arguments request: ListJdRecommendationRequest,
    ): JdRecommendationListResponse = JdRecommendationListResponse.from(
        getJdRecommendationService.getRecommendations(tags, request.cursor, request.size),
    )
}
