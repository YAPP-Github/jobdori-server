package com.jobdori.api.application.experience.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.application.experience.service.ExperiencePdfImportService
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.core.application.auth.AccessTokenService
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

@DocsTest
@ApiTest(ExperienceImportPdfController::class)
internal class ExperienceImportPdfControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val experiencePdfImportService: ExperiencePdfImportService,
) : StringSpec({

    "PDF 파일에서 경험을 가져온다" {
        val file = MockMultipartFile(
            "file",
            "resume.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "%PDF-1.4 sample".toByteArray(),
        )

        every { accessTokenService.getUserId("access-token") } returns 1L
        every {
            experiencePdfImportService.importExperiences(
                file = any(),
                workspaceId = "workspace-id",
                userId = 1L,
            )
        } returns Unit

        mockMvc.multipart("/v1/workspaces/{workspaceId}/experience-imports", "workspace-id") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            file(file)
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
        }.andDo {
            handle(
                document(
                    "experience-import-pdf",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                    ),
                    requestParts(
                        partWithName("file").description("경험을 가져올 PDF 파일"),
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                    ),
                ),
            )
        }

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) {
            experiencePdfImportService.importExperiences(
                file = file,
                workspaceId = "workspace-id",
                userId = 1L,
            )
        }
    }

})
