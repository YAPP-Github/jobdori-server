package com.jobdori.api.application.user.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.support.docs.ErrorCodeSnippet
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.error.UserErrorCode
import com.jobdori.core.domain.user.service.UserReader
import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.service.WorkspaceReader
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
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val userReader: UserReader,
    @MockkBean
    private val workspaceReader: WorkspaceReader,
) : StringSpec({

    "인증된 사용자 정보를 조회한다" {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every { userReader.getUser(1L) } returns User(
            id = 1L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            name = "홍길동",
            profileImageUrl = "https://lh3.googleusercontent.com/profile",
        )
        every { workspaceReader.getWorkspaces(ownerUserId = 1L) } returns listOf(
            Workspace(
                id = 10L,
                publicId = "8f13f49e-132a-47b7-b704-d7eec18fd44b",
                ownerUserId = 1L,
            ),
        )

        mockMvc.get("/v1/users/me") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.userId") { value("3f5c9d79-2255-4b76-bd31-013cd01d49d6") }
            jsonPath("$.result.name") { value("홍길동") }
            jsonPath("$.result.profileImageUrl") { value("https://lh3.googleusercontent.com/profile") }
            jsonPath("$.result.workspaces[0].workspaceId") { value("8f13f49e-132a-47b7-b704-d7eec18fd44b") }
        }.andDo {
            handle(
                document(
                    "user-me",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        fieldWithPath("result.userId").type(JsonFieldType.STRING).description("사용자 ID"),
                        fieldWithPath("result.name").type(JsonFieldType.STRING).description("사용자 이름"),
                        fieldWithPath("result.profileImageUrl").type(JsonFieldType.STRING)
                            .description("사용자 프로필 이미지 URL").optional(),
                        fieldWithPath("result.workspaces").type(JsonFieldType.ARRAY).description("사용자가 속한 워크스페이스 목록"),
                        fieldWithPath("result.workspaces[].workspaceId").type(JsonFieldType.STRING).description("워크스페이스 ID"),
                    ),
                    ErrorCodeSnippet.errorCodeSnippet(
                        UserErrorCode.E404_USER_NOT_FOUND,
                    ),
                ),
            )
        }

        verify(exactly = 1) { userReader.getUser(1L) }
        verify(exactly = 1) { workspaceReader.getWorkspaces(ownerUserId = 1L) }
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
