package com.jobdori.api.application.consent.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.user.controller.UserQueryResolver
import com.jobdori.api.application.user.dto.response.UserResponse
import com.jobdori.api.application.user.service.UserService
import com.jobdori.api.application.workspace.service.WorkspaceService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.consent.UserConsentService
import com.jobdori.core.domain.consent.ConsentType
import com.jobdori.core.domain.consent.ConsentSource
import com.jobdori.core.domain.consent.UserConsent
import com.jobdori.core.domain.consent.error.ConsentAlreadyAgreedException
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity
import java.time.LocalDateTime

@GraphQLTest(ConsentResolver::class, UserQueryResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class ConsentResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean private val accessTokenService: AccessTokenService,
    @MockkBean private val userService: UserService,
    @MockkBean private val workspaceService: WorkspaceService,
    @MockkBean private val userConsentService: UserConsentService,
) : StringSpec({

    "사용자의 동의 상태와 시트 노출 여부를 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userService.getMe(1L) } returns user()
        every { userConsentService.getConsents(1L) } returns listOf(consent(ConsentType.TERMS, true))
        every { userConsentService.isConsentSheetRequired(1L) } returns true

        authenticatedTester(graphQlTester).documentName("consents").execute()
            .path("me.consents[0].type").entity<String>().isEqualTo("TERMS")
            .path("me.consents[0].agreed").entity<Boolean>().isEqualTo(true)
            .path("me.consentSheetRequired").entity<Boolean>().isEqualTo(true)
    }

    "가입 동의를 저장한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userConsentService.agreeSignupConsents(1L, true, false) } returns listOf(consent(ConsentType.TERMS, true))

        authenticatedTester(graphQlTester).documentName("agree-signup-consents")
            .variable("request", mapOf("terms" to true, "privacy" to true, "research" to true, "marketingEmail" to false))
            .execute().path("agreeSignupConsents[0].type").entity<String>().isEqualTo("TERMS")
    }

    "필수 약관에 동의하지 않으면 가입 동의를 거부한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L

        authenticatedTester(graphQlTester).documentName("agree-signup-consents")
            .variable("request", mapOf("terms" to false, "privacy" to true))
            .execute().errors().satisfy { errors ->
                errors shouldHaveSize 1
                errors.first().extensions shouldContain ("code" to "invalid_arguments")
            }
        verify(exactly = 0) { userConsentService.agreeSignupConsents(any(), any(), any()) }
    }

    "이미 가입 동의 이력이 있으면 가입 동의를 거부한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userConsentService.agreeSignupConsents(1L, null, null) } throws ConsentAlreadyAgreedException()

        authenticatedTester(graphQlTester).documentName("agree-signup-consents")
            .variable("request", mapOf("terms" to true, "privacy" to true))
            .execute().errors().satisfy { errors ->
                errors shouldHaveSize 1
                errors.first().extensions shouldContain ("code" to "consent_already_agreed")
            }
    }

    "선택 동의 상태를 변경한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userConsentService.updateOptionalConsents(1L, false, null, any()) } returns
            listOf(consent(ConsentType.RESEARCH, false))

        authenticatedTester(graphQlTester).documentName("update-optional-consents")
            .variable("request", mapOf("research" to false, "source" to "MYPAGE"))
            .execute().path("updateOptionalConsents[0].agreed").entity<Boolean>().isEqualTo(false)
    }

    "선택 동의 항목이 없으면 변경을 거부한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L

        authenticatedTester(graphQlTester).documentName("update-optional-consents")
            .variable("request", mapOf("source" to "MYPAGE"))
            .execute().errors().satisfy { errors ->
                errors shouldHaveSize 1
                errors.first().extensions shouldContain ("code" to "invalid_arguments")
            }
        verify(exactly = 0) { userConsentService.updateOptionalConsents(any(), any(), any(), any()) }
    }

    "동의 시트 노출을 기록한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userConsentService.markConsentSheetShown(1L) } returns true

        authenticatedTester(graphQlTester).documentName("mark-consent-sheet-shown").execute()
            .path("markConsentSheetShown").entity<Boolean>().isEqualTo(true)
    }
})

private fun user() = UserResponse(
    id = 1L,
    userId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
    email = "hong@example.com",
    name = "홍길동",
    profileImageUrl = null,
)

private fun consent(type: ConsentType, agreed: Boolean) = UserConsent(
    id = 1L,
    userId = 1L,
    type = type,
    agreed = agreed,
    source = ConsentSource.SIGNUP,
    createdAt = LocalDateTime.parse("2026-01-01T00:00:00"),
)

private fun authenticatedTester(graphQlTester: GraphQlTester): GraphQlTester {
    val builder = graphQlTester.mutate() as ExecutionGraphQlServiceTester.Builder<*>
    return builder.configureExecutionInput { _, executionInputBuilder ->
        executionInputBuilder.graphQLContext(mapOf(AuthGraphQlContext.AUTHORIZATION to "Bearer access-token")).build()
    }.build()
}
