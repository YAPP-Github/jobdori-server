package com.jobdori.api.application.experience.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.application.common.dto.request.PeriodRequest
import com.jobdori.api.application.experience.dto.request.CreateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.service.ExperienceProjectService
import com.jobdori.api.support.docs.ErrorCodeSnippet
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.error.ErrorCode
import com.jobdori.common.json.JsonUtils
import com.jobdori.common.model.Period
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.error.ExperienceProjectErrorCode
import com.jobdori.core.domain.workspace.error.WorkspaceErrorCode
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.math.BigDecimal
import java.time.LocalDate

@DocsTest
@ApiTest(ExperienceProjectController::class)
internal class ExperienceProjectControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val experienceProjectService: ExperienceProjectService,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every {
            experienceProjectService.createProject(
                userId = any(),
                workspaceId = any(),
                name = any(),
                summary = any(),
                period = any(),
                role = any(),
            )
        } answers {
            ExperienceProjectResponse.from(
                experienceProject(
                    id = 100L,
                    role = arg(5),
                    period = arg(4),
                ),
            )
        }
        every {
            experienceProjectService.modifyProject(
                userId = any(),
                workspaceId = any(),
                projectId = any(),
                name = any(),
                summary = any(),
                period = any(),
                role = any(),
            )
        } answers {
            ExperienceProjectResponse.from(
                experienceProject(
                    id = arg(2),
                    role = arg<String?>(6) ?: "Growth Marketer",
                    period = arg<Period?>(5) ?: Period(
                        startAt = LocalDate.of(2025, 1, 1),
                        endAt = LocalDate.of(2025, 4, 30),
                    ),
                ),
            )
        }
        every { experienceProjectService.removeProject(any(), any(), any()) } returns Unit
    }

    "경험 프로젝트를 생성한다" {
        mockMvc.post("/v1/workspaces/{workspaceId}/experience-projects", "workspace-id") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                CreateExperienceProjectRequest(
                    name = "신규 브랜드 런칭 캠페인",
                    summary = "신규 서비스의 초기 인지도 확보 캠페인",
                    period = PeriodRequest(
                        startAt = LocalDate.of(2025, 1, 1),
                        endAt = LocalDate.of(2025, 4, 30),
                    ),
                    role = "Growth Marketer",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.projectId") { value(100) }
            jsonPath("$.result.name") { value("신규 브랜드 런칭 캠페인") }
        }.andDo {
            handle(
                document(
                    "experience-project-create",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                    ),
                    experienceProjectRequestFields(),
                    experienceProjectResponseFields(),
                    experienceProjectErrorCodeSnippet(),
                ),
            )
        }

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) {
            experienceProjectService.createProject(
                userId = 1L,
                workspaceId = "workspace-id",
                name = "신규 브랜드 런칭 캠페인",
                summary = "신규 서비스의 초기 인지도 확보 캠페인",
                period = Period(
                    startAt = LocalDate.of(2025, 1, 1),
                    endAt = LocalDate.of(2025, 4, 30),
                ),
                role = "Growth Marketer",
            )
        }
    }

    "경험 프로젝트 기간은 시작일이 종료일보다 늦을 수 없다" {
        mockMvc.post("/v1/workspaces/{workspaceId}/experience-projects", "workspace-id") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                CreateExperienceProjectRequest(
                    name = "신규 브랜드 런칭 캠페인",
                    summary = "신규 서비스의 초기 인지도 확보 캠페인",
                    period = PeriodRequest(
                        startAt = LocalDate.of(2025, 5, 1),
                        endAt = LocalDate.of(2025, 4, 30),
                    ),
                    role = "Growth Marketer",
                ),
            )
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.ok") { value(false) }
            jsonPath("$.error.code") { value(CommonErrorCode.E400_INVALID_ARGUMENTS.code) }
            jsonPath("$.error.details[0].field") { value("period.validPeriod") }
            jsonPath("$.error.details[0].reason") { value("startAt must be before or equal to endAt") }
        }
    }

    "경험 프로젝트를 수정한다" {
        mockMvc.patch("/v1/workspaces/{workspaceId}/experience-projects/{projectId}", "workspace-id", 3L) {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                UpdateExperienceProjectRequest(
                    role = "Brand Growth Lead",
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.projectId") { value(3) }
            jsonPath("$.result.name") { value("신규 브랜드 런칭 캠페인") }
            jsonPath("$.result.role") { value("Brand Growth Lead") }
        }.andDo {
            handle(
                document(
                    "experience-project-update",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                        parameterWithName("projectId").description("프로젝트 ID"),
                    ),
                    experienceProjectUpdateRequestFields(),
                    experienceProjectResponseFields(),
                    experienceProjectErrorCodeSnippet(
                        ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND,
                    ),
                ),
            )
        }
    }

    "경험 프로젝트를 삭제한다" {
        mockMvc.delete("/v1/workspaces/{workspaceId}/experience-projects/{projectId}", "workspace-id", 3L) {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result") { doesNotExist() }
        }.andDo {
            handle(
                document(
                    "experience-project-delete",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                        parameterWithName("projectId").description("프로젝트 ID"),
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                    ),
                    experienceProjectErrorCodeSnippet(
                        ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND,
                    ),
                ),
            )
        }
    }

})

private fun experienceProjectRequestFields() = requestFields(
    fieldWithPath("name").type(JsonFieldType.STRING).description("프로젝트 이름")
        .attributes(RestDocsUtils.remarks("공백 불가, 최대 100자")),
    fieldWithPath("summary").type(JsonFieldType.STRING).description("프로젝트 요약")
        .attributes(RestDocsUtils.remarks("공백 불가, 최대 500자")),
    fieldWithPath("period").type(JsonFieldType.OBJECT).description("프로젝트 진행 기간").optional()
        .attributes(RestDocsUtils.remarks("startAt은 endAt보다 늦을 수 없음")),
    fieldWithPath("period.startAt").type(JsonFieldType.STRING).description("프로젝트 시작일").optional(),
    fieldWithPath("period.endAt").type(JsonFieldType.STRING).description("프로젝트 종료일").optional(),
    fieldWithPath("role").type(JsonFieldType.STRING).description("프로젝트에서 맡은 역할").optional()
        .attributes(RestDocsUtils.remarks("값이 있으면 공백 불가, 최대 100자")),
)

private fun experienceProjectUpdateRequestFields() = requestFields(
    fieldWithPath("name").type(JsonFieldType.STRING).description("변경할 프로젝트 이름").optional()
        .attributes(RestDocsUtils.remarks("값이 있으면 공백 불가, 최대 100자")),
    fieldWithPath("summary").type(JsonFieldType.STRING).description("변경할 프로젝트 요약").optional()
        .attributes(RestDocsUtils.remarks("값이 있으면 공백 불가, 최대 500자")),
    fieldWithPath("period").type(JsonFieldType.OBJECT).description("변경할 프로젝트 진행 기간").optional()
        .attributes(RestDocsUtils.remarks("startAt은 endAt보다 늦을 수 없음")),
    fieldWithPath("period.startAt").type(JsonFieldType.STRING).description("변경할 프로젝트 시작일").optional(),
    fieldWithPath("period.endAt").type(JsonFieldType.STRING).description("변경할 프로젝트 종료일").optional(),
    fieldWithPath("role").type(JsonFieldType.STRING).description("변경할 프로젝트에서 맡은 역할").optional()
        .attributes(RestDocsUtils.remarks("값이 있으면 공백 불가, 최대 100자")),
)

private fun experienceProjectResponseFields() = responseFields(
    fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
    fieldWithPath("result.projectId").type(JsonFieldType.NUMBER).description("프로젝트 ID"),
    fieldWithPath("result.name").type(JsonFieldType.STRING).description("프로젝트 이름"),
    fieldWithPath("result.summary").type(JsonFieldType.STRING).description("프로젝트 요약"),
    fieldWithPath("result.period").type(JsonFieldType.OBJECT).description("프로젝트 진행 기간").optional(),
    fieldWithPath("result.period.startAt").type(JsonFieldType.STRING).description("프로젝트 시작일").optional(),
    fieldWithPath("result.period.endAt").type(JsonFieldType.STRING).description("프로젝트 종료일").optional(),
    fieldWithPath("result.role").type(JsonFieldType.STRING).description("프로젝트에서 맡은 역할").optional(),
)

private fun experienceProjectErrorCodeSnippet(
    vararg errorCodes: ErrorCode,
) = ErrorCodeSnippet.errorCodeSnippet(
    WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED to WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED.description,
    WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND to WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND.description,
    *errorCodes.map { it to it.description }.toTypedArray(),
)

private fun experienceProject(
    id: Long,
    role: String?,
    period: Period?,
) = ExperienceProject(
    id = id,
    workspaceId = 1L,
    name = "신규 브랜드 런칭 캠페인",
    summary = "신규 서비스의 초기 인지도 확보 캠페인",
    period = period,
    role = role,
    displayOrder = BigDecimal.ZERO,
    status = ExperienceProjectStatus.ACTIVE,
)
