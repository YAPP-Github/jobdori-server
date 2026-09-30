package com.jobdori.api.application.resume.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.api.application.resume.dto.response.ResumeResponse
import com.jobdori.api.application.resume.service.PdfResumeImportService
import com.jobdori.api.support.docs.ErrorCodeSnippet
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.common.error.FileErrorCode
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.workspace.error.WorkspaceErrorCode
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import java.time.Instant

@DocsTest
@ApiTest(PdfResumeImportController::class)
internal class PdfResumeImportControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val pdfResumeImportService: PdfResumeImportService,
) : StringSpec({

    "PDF 파일에서 이력서를 가져온다" {
        val file = MockMultipartFile(
            "file",
            "resume.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "%PDF-1.4 sample".toByteArray(),
        )
        val response = ResumeResponse(
            resumeId = 100L,
            targetJd = null,
            template = ResumeTemplate.DEFAULT,
            status = ResumeStatusType.DRAFT,
            sections = emptyList(),
            createdAt = Instant.parse("2026-10-01T00:00:00Z"),
        )

        every { accessTokenService.getUserId("access-token") } returns 1L
        every {
            pdfResumeImportService.importResume(
                file = any(),
                workspaceId = "workspace-id",
                userId = 1L,
            )
        } returns response

        mockMvc.multipart("/v1/workspaces/{workspaceId}/resume-imports", "workspace-id") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            file(file)
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.resumeId") { value(100) }
            jsonPath("$.result.template") { value("DEFAULT") }
            jsonPath("$.result.status") { value("DRAFT") }
        }.andDo {
            handle(
                document(
                    "resume-import-from-file",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                    ),
                    requestParts(
                        partWithName("file").description("이력서를 가져올 PDF 파일"),
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        fieldWithPath("result.resumeId").type(JsonFieldType.NUMBER).description("생성된 이력서 ID"),
                        fieldWithPath("result.template").type(JsonFieldType.STRING).description("이력서 템플릿"),
                        fieldWithPath("result.status").type(JsonFieldType.STRING).description("이력서 상태"),
                        fieldWithPath("result.sections").type(JsonFieldType.ARRAY).description("이력서 섹션 목록"),
                        fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("이력서 생성 시각"),
                    ),
                    ErrorCodeSnippet.errorCodeSnippet(
                        FileErrorCode.E400_FILE_SIZE_EXCEEDED to "업로드한 파일 크기가 서버에서 허용한 최대 크기를 초과한 경우",
                        WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED to "요청 사용자가 해당 워크스페이스에 접근할 권한이 없는 경우",
                        WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND to "요청한 워크스페이스를 찾을 수 없는 경우",
                    ),
                ),
            )
        }

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) {
            pdfResumeImportService.importResume(
                file = file,
                workspaceId = "workspace-id",
                userId = 1L,
            )
        }
    }
})
