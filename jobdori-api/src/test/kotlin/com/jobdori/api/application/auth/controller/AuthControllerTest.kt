package com.jobdori.api.application.auth.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.application.auth.dto.request.LoginRequest
import com.jobdori.api.support.docs.ErrorCodeSnippet
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.api.support.docs.RestDocsUtils.convertToString
import com.jobdori.api.support.docs.RestDocsUtils.remarks
import com.jobdori.common.json.JsonUtils
import com.jobdori.core.application.auth.AuthService
import com.jobdori.core.application.auth.RefreshTokenService
import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.result.AuthResult
import com.jobdori.core.domain.auth.AuthToken
import com.jobdori.core.domain.auth.AuthTokenPair
import com.jobdori.core.domain.auth.error.AuthErrorCode
import com.jobdori.core.domain.user.UserIdentityProvider
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.justRun
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockCookie
import org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName
import org.springframework.restdocs.cookies.CookieDocumentation.requestCookies
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant

@DocsTest
@ApiTest(AuthController::class)
internal class AuthControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean
    private val authService: AuthService,
    @MockkBean
    private val refreshTokenService: RefreshTokenService,
) : StringSpec({

    val command = AuthCommand(
        provider = UserIdentityProvider.GOOGLE,
        authorizationCode = "authorization-code",
        redirectUri = "https://jobdori.com/auth/callback",
    )

    "로그인 후 인증 토큰을 발급한다" {
        every { authService.login(command) } returns AuthResult(
            isNewUser = false,
            tokenPair = AuthTokenPair(
                accessToken = AuthToken(
                    value = "access-token",
                    tokenId = "access-token-id",
                    expiresAt = Instant.parse("2030-01-01T00:30:00Z"),
                ),
                refreshToken = AuthToken(
                    value = "refresh-token",
                    tokenId = "refresh-token-id",
                    expiresAt = Instant.parse("2030-01-15T00:00:00Z"),
                ),
            ),
        )

        mockMvc.post("/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                LoginRequest(
                    provider = UserIdentityProvider.GOOGLE,
                    authorizationCode = "authorization-code",
                    redirectUri = "https://jobdori.com/auth/callback",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.accessToken") { value("access-token") }
            jsonPath("$.result.refreshToken") { value("refresh-token") }
            jsonPath("$.result.isNewUser") { value(false) }
            cookie { value("access_token", "access-token") }
            cookie { value("refresh_token", "refresh-token") }
        }.andDo {
            handle(
                document(
                    "auth-login",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    requestFields(
                        fieldWithPath("provider").type(JsonFieldType.STRING).description("인증 제공자")
                            .attributes(remarks(convertToString(UserIdentityProvider::class.java))),
                        fieldWithPath("authorizationCode").type(JsonFieldType.STRING).description("OAuth 인가 코드"),
                        fieldWithPath("redirectUri").type(JsonFieldType.STRING).description("OAuth 리다이렉트 URI"),
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        fieldWithPath("result.isNewUser").type(JsonFieldType.BOOLEAN).description("신규 가입 여부"),
                        fieldWithPath("result.accessToken").type(JsonFieldType.STRING).description("Access 토큰"),
                        fieldWithPath("result.refreshToken").type(JsonFieldType.STRING).description("Refresh 토큰"),
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("`access_token`, `refresh_token` 쿠키"),
                    ),
                    ErrorCodeSnippet.errorCodeSnippet(
                        AuthErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE to AuthErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE.description,
                    ),
                ),
            )
        }
    }

    "Refresh 토큰으로 Access 토큰을 재발급한다" {
        every { refreshTokenService.refresh("refresh-token") } returns AuthToken(
            value = "new-access-token",
            tokenId = "new-access-token-id",
            expiresAt = Instant.parse("2030-01-01T00:30:00Z"),
        )

        mockMvc.post("/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            cookie(MockCookie("refresh_token", "refresh-token"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.accessToken") { value("new-access-token") }
            jsonPath("$.result.refreshToken") { doesNotExist() }
            cookie { value("access_token", "new-access-token") }
        }.andDo {
            handle(
                document(
                    "auth-refresh",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    requestCookies(
                        cookieWithName("refresh_token").description("Refresh 토큰"),
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        fieldWithPath("result.accessToken").type(JsonFieldType.STRING).description("Access 토큰"),
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("`access_token` 쿠키"),
                    ),
                ),
            )
        }
    }

    "로그아웃 시 인증 쿠키를 만료한다" {
        justRun { refreshTokenService.validate("refresh-token") }

        mockMvc.post("/v1/auth/logout") {
            contentType = MediaType.APPLICATION_JSON
            cookie(MockCookie("refresh_token", "refresh-token"))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.ok") { value(true) }
                jsonPath("$.result") { doesNotExist() }
                cookie { maxAge("access_token", 0) }
                cookie { maxAge("refresh_token", 0) }
            }.andDo {
                handle(
                    document(
                        "auth-logout",
                        RestDocsUtils.getDocumentRequest(),
                        RestDocsUtils.getDocumentResponse(),
                        PageHeaderSnippet.pageHeaderSnippet(),
                        requestCookies(
                            cookieWithName("refresh_token").description("Refresh 토큰"),
                        ),
                        responseFields(
                            fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        ),
                        responseHeaders(
                            headerWithName(HttpHeaders.SET_COOKIE).description("만료된 `access_token`, `refresh_token` 쿠키"),
                        ),
                    ),
                )
            }
    }

})
