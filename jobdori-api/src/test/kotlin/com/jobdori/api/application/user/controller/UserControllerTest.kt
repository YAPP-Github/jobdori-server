package com.jobdori.api.application.user.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.support.docs.ErrorCodeSnippet
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.core.application.auth.AuthUserReadService
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.error.UserErrorCode
import com.jobdori.core.domain.user.service.UserReader
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@DocsTest
@ApiTest(UserController::class)
internal class UserControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean
    private val authUserReadService: AuthUserReadService,
    @MockkBean
    private val userReader: UserReader,
) : StringSpec({

    "인증된 사용자 정보를 조회한다" {
        every { authUserReadService.getUserId("access-token") } returns 1L
        every { userReader.getUser(1L) } returns User(
            id = 1L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )

        mockMvc.get("/v1/users/me") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.userId") { value("3f5c9d79-2255-4b76-bd31-013cd01d49d6") }
        }.andDo {
            handle(
                document(
                    "user-me",
                    RestDocsUtils.getDocumentRequest(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        fieldWithPath("result.userId").type(JsonFieldType.STRING).description("사용자 ID(UUID)"),
                    ),
                    ErrorCodeSnippet.errorCodeSnippet(
                        UserErrorCode.E404_USER_NOT_FOUND,
                    ),
                ),
            )
        }

        verify(exactly = 1) { userReader.getUser(1L) }
    }

    "인증 토큰이 없으면 사용자 정보를 조회할 수 없다" {
        mockMvc.get("/v1/users/me")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.ok") { value(false) }
                jsonPath("$.error.code") { value("invalid_auth_token") }
            }
    }

})
