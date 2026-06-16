package com.jobdori.api.application.auth.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.application.auth.dto.request.SignUpRequest
import com.jobdori.api.support.docs.ErrorCodeSnippet
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.api.support.docs.RestDocsUtils.convertToString
import com.jobdori.api.support.docs.RestDocsUtils.remarks
import com.jobdori.common.json.JsonUtils
import com.jobdori.core.application.auth.LoginService
import com.jobdori.core.application.auth.RefreshAccessTokenService
import com.jobdori.core.application.auth.SignUpService
import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.error.AuthErrorCode
import com.jobdori.core.application.auth.token.AuthToken
import com.jobdori.core.application.auth.token.AuthTokenPair
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.error.UserErrorCode
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
    private val signUpService: SignUpService,
    @MockkBean
    private val loginService: LoginService,
    @MockkBean
    private val refreshAccessTokenService: RefreshAccessTokenService,
) : StringSpec({

    val command = AuthCommand(
        provider = UserIdentifyProvider.GOOGLE,
        authorizationCode = "authorization-code",
    )

    "회원가입 후 인증 토큰을 발급한다" {
        every { signUpService.signUp(command) } returns AuthTokenPair(
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
        )

        mockMvc.post("/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                SignUpRequest(
                    provider = UserIdentifyProvider.GOOGLE,
                    authorizationCode = "authorization-code",
                ),
            )

        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.accessToken") { value("access-token") }
            jsonPath("$.result.refreshToken") { value("refresh-token") }
            cookie { value("access_token", "access-token") }
            cookie { value("refresh_token", "refresh-token") }
        }.andDo {
            handle(
                document(
                    "auth-signup",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    requestFields(
                        fieldWithPath("provider").type(JsonFieldType.STRING).description("인증 제공자")
                            .attributes(remarks(convertToString(UserIdentifyProvider::class.java))),
                        fieldWithPath("authorizationCode").type(JsonFieldType.STRING).description("OAuth 인가 코드")
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        fieldWithPath("result.accessToken").type(JsonFieldType.STRING).description("Access 토큰"),
                        fieldWithPath("result.refreshToken").type(JsonFieldType.STRING).description("Refresh 토큰"),
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("`access_token`, `refresh_token` 쿠키"),
                    ),
                    ErrorCodeSnippet.errorCodeSnippet(AuthErrorCode.E409_ALREADY_SIGNED_UP),
                ),
            )
        }
    }

    "로그인 후 인증 토큰을 발급한다" {
        every { loginService.login(command) } returns AuthTokenPair(
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
        )

        mockMvc.post("/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                SignUpRequest(
                    provider = UserIdentifyProvider.GOOGLE,
                    authorizationCode = "authorization-code",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.accessToken") { value("access-token") }
            jsonPath("$.result.refreshToken") { value("refresh-token") }
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
                            .attributes(remarks(convertToString(UserIdentifyProvider::class.java))),
                        fieldWithPath("authorizationCode").type(JsonFieldType.STRING).description("OAuth 인가 코드")
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        fieldWithPath("result.accessToken").type(JsonFieldType.STRING).description("Access 토큰"),
                        fieldWithPath("result.refreshToken").type(JsonFieldType.STRING).description("Refresh 토큰"),
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("`access_token`, `refresh_token` 쿠키"),
                    ),
                    ErrorCodeSnippet.errorCodeSnippet(UserErrorCode.E404_USER_NOT_FOUND to "가입된 사용자가 아닌 경우"),
                ),
            )
        }
    }

    "Refresh 토큰으로 Access 토큰을 재발급한다" {
        every { refreshAccessTokenService.refresh("refresh-token") } returns AuthToken(
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
        justRun { refreshAccessTokenService.validate("refresh-token") }

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
