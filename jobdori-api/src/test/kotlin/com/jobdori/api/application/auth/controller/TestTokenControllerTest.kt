package com.jobdori.api.application.auth.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.support.docs.ErrorCodeSnippet
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.core.domain.auth.AuthToken
import com.jobdori.core.domain.auth.AuthTokenPair
import com.jobdori.core.domain.auth.service.AuthTokenProvider
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.repository.UserRepository
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime
import java.time.ZoneId

@DocsTest
@ApiTest(TestTokenController::class)
internal class TestTokenControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean
    private val userRepository: UserRepository,
    @MockkBean
    private val authTokenProvider: AuthTokenProvider,
) : StringSpec({

    "샘플 유저가 없으면 생성하고 테스트 토큰을 발급한다" {
        val accessTokenExpiresAt = LocalDateTime.parse("2030-01-01T00:00:00")
        val refreshTokenExpiresAt = LocalDateTime.parse("2030-01-15T00:00:00")
        val accessTokenExpiresAtInstant = accessTokenExpiresAt.atZone(ZoneId.systemDefault()).toInstant()
        val refreshTokenExpiresAtInstant = refreshTokenExpiresAt.atZone(ZoneId.systemDefault()).toInstant()
        val user = User(
            id = 1L,
            publicId = "00000000-0000-0000-0000-000000000001",
        )

        every { userRepository.findByPublicId(user.publicId) } returns null
        every { userRepository.save(User(id = 0L, publicId = user.publicId)) } returns user
        every {
            authTokenProvider.issue(
                userPublicId = user.publicId,
                accessTokenExpiresAt = accessTokenExpiresAtInstant,
                refreshTokenExpiresAt = refreshTokenExpiresAtInstant,
            )
        } returns AuthTokenPair(
            accessToken = AuthToken(
                value = "access-token",
                tokenId = "access-token-id",
                expiresAt = accessTokenExpiresAtInstant,
            ),
            refreshToken = AuthToken(
                value = "refresh-token",
                tokenId = "refresh-token-id",
                expiresAt = refreshTokenExpiresAtInstant,
            ),
        )

        mockMvc.get("/test-tokens") {
            param("accessTokenExpiresAt", accessTokenExpiresAt.toString())
            param("refreshTokenExpiresAt", refreshTokenExpiresAt.toString())
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
                    "dev-test-token",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    queryParameters(
                        parameterWithName("accessTokenExpiresAt").optional()
                            .description("테스트 Access 토큰 만료 시각. ISO local date-time 형식이며 생략 시 서버 기본값을 사용합니다."),
                        parameterWithName("refreshTokenExpiresAt").optional()
                            .description("테스트 Refresh 토큰 만료 시각. ISO local date-time 형식이며 생략 시 서버 기본값을 사용합니다."),
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        fieldWithPath("result.accessToken").type(JsonFieldType.STRING).description("테스트 Access 토큰"),
                        fieldWithPath("result.refreshToken").type(JsonFieldType.STRING).description("테스트 Refresh 토큰"),
                    ),
                    responseHeaders(
                        headerWithName(HttpHeaders.SET_COOKIE).description("`access_token`, `refresh_token` 쿠키"),
                    ),
                    ErrorCodeSnippet.errorCodeSnippet(CommonErrorCode.E400_INVALID_ARGUMENTS to "만료 시간이 현재보다 이전인 경우"),
                ),
            )
        }

        verify(exactly = 1) { userRepository.save(User(id = 0L, publicId = user.publicId)) }
    }

})
