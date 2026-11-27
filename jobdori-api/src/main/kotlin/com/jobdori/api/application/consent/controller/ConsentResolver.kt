package com.jobdori.api.application.consent.controller

import com.jobdori.api.application.consent.dto.request.AgreeSignupConsentsRequest
import com.jobdori.api.application.consent.dto.request.UpdateOptionalConsentsRequest
import com.jobdori.api.application.consent.dto.response.UserConsentResponse
import com.jobdori.api.application.user.dto.response.UserResponse
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.consent.UserConsentService
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ConsentResolver(
    private val userConsentService: UserConsentService,
) {

    @SchemaMapping(typeName = "User", field = "consents")
    fun consents(user: UserResponse): List<UserConsentResponse> =
        userConsentService.getConsents(user.id).map(UserConsentResponse::from)

    @SchemaMapping(typeName = "User", field = "consentSheetRequired")
    fun consentSheetRequired(user: UserResponse): Boolean = userConsentService.isConsentSheetRequired(user.id)

    @MutationMapping
    fun agreeSignupConsents(
        @UserId userId: Long,
        @Valid @Argument request: AgreeSignupConsentsRequest,
    ): List<UserConsentResponse> = userConsentService.agreeSignupConsents(
        userId = userId,
        research = request.research,
        marketingEmail = request.marketingEmail,
    ).map(UserConsentResponse::from)

    @MutationMapping
    fun updateOptionalConsents(
        @UserId userId: Long,
        @Valid @Argument request: UpdateOptionalConsentsRequest,
    ): List<UserConsentResponse> = userConsentService.updateOptionalConsents(
        userId = userId,
        research = request.research,
        marketingEmail = request.marketingEmail,
        source = request.source,
    ).map(UserConsentResponse::from)

    @MutationMapping
    fun markConsentSheetShown(@UserId userId: Long): Boolean = userConsentService.markConsentSheetShown(userId)
}
