package com.jobdori.api.application.experience.controller

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.application.experience.dto.request.CreateExperienceRequest
import com.jobdori.api.application.experience.dto.request.contents.ExperienceContentsRequest
import com.jobdori.api.application.experience.dto.request.contents.FreeExperienceContentsRequest
import com.jobdori.api.application.experience.dto.request.contents.StarExperienceContentsRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceRequest
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.experience.service.ExperienceService
import com.jobdori.api.support.docs.ErrorCodeSnippet
import com.jobdori.api.support.docs.PageHeaderSnippet
import com.jobdori.api.support.docs.RestDocsUtils
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.error.ErrorCode
import com.jobdori.common.json.JsonUtils
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceContentsType
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.error.ExperienceErrorCode
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

@DocsTest
@ApiTest(ExperienceController::class)
internal class ExperienceControllerTest(
    private val mockMvc: MockMvc,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val experienceService: ExperienceService,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
        every {
            experienceService.createExperience(
                userId = any(),
                workspaceId = any(),
                projectId = any(),
                tags = any(),
                title = any(),
                contents = any(),
            )
        } answers {
            ExperienceResponse.from(
                experience = experience(
                    id = 100L,
                    projectId = arg(2),
                    tags = arg(3),
                    title = arg(4),
                    contents = arg(5),
                ),
                project = ExperienceProjectResponse.from(project(id = arg(2))),
            )
        }
        every {
            experienceService.modifyExperience(
                userId = any(),
                workspaceId = any(),
                experienceId = any(),
                projectId = any(),
                tags = any(),
                title = any(),
                contents = any(),
            )
        } answers {
            val projectId = arg<Long?>(3) ?: 3L
            ExperienceResponse.from(
                experience = experience(
                    id = arg(2),
                    projectId = projectId,
                    tags = arg<List<String>?>(4) ?: listOf("브랜드런칭", "퍼포먼스마케팅"),
                    title = arg<String?>(5) ?: "런칭 캠페인 메시지 A/B 테스트",
                    contents = arg<ExperienceContents?>(6) ?: ExperienceContents.free(""),
                ),
                project = ExperienceProjectResponse.from(project(id = projectId)),
            )
        }
        every { experienceService.removeExperience(any(), any<String>(), any()) } returns Unit
    }

    "경험을 생성한다" {
        mockMvc.post("/v1/workspaces/{workspaceId}/experiences", "workspace-id") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                CreateExperienceRequest(
                    projectId = 3L,
                    tags = listOf("브랜드런칭", "퍼포먼스마케팅"),
                    title = "런칭 캠페인 메시지 A/B 테스트",
                    contents = ExperienceContentsRequest(
                        type = ExperienceContentsType.STAR,
                        star = StarExperienceContentsRequest(
                            situation = "신규 브랜드 런칭 직후 핵심 메시지에 대한 반응 데이터가 부족했습니다.",
                            task = "제한된 예산 안에서 전환 가능성이 높은 메시지를 빠르게 찾아야 했습니다.",
                            action = "문제 인식형, 혜택 강조형, 사회적 증거형 메시지로 광고 소재와 랜딩 카피를 나눠 테스트했습니다.",
                            result = "혜택 강조형 메시지의 클릭률과 회원가입 전환율이 가장 높아 메인 캠페인 메시지로 확정했습니다.",
                        ),
                    ),
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.experienceId") { value(100) }
            jsonPath("$.result.project.projectId") { value(3) }
            jsonPath("$.result.title") { value("런칭 캠페인 메시지 A/B 테스트") }
            jsonPath("$.result.contents.type") { value("STAR") }
            jsonPath("$.result.contents.star.situation") {
                value("신규 브랜드 런칭 직후 핵심 메시지에 대한 반응 데이터가 부족했습니다.")
            }
        }.andDo {
            handle(
                document(
                    "experience-create",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                    ),
                    experienceRequestFields(),
                    experienceResponseFields(),
                    experienceErrorCodeSnippet(
                        ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND to
                            "연결할 경험 프로젝트를 찾을 수 없는 경우",
                    ),
                ),
            )
        }

        verify(exactly = 1) { accessTokenService.getUserId("access-token") }
        verify(exactly = 1) {
            experienceService.createExperience(
                userId = 1L,
                workspaceId = "workspace-id",
                projectId = 3L,
                tags = listOf("브랜드런칭", "퍼포먼스마케팅"),
                title = "런칭 캠페인 메시지 A/B 테스트",
                contents = any(),
            )
        }
    }

    "FREE 타입 경험을 생성한다" {
        mockMvc.post("/v1/workspaces/{workspaceId}/experiences", "workspace-id") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                CreateExperienceRequest(
                    projectId = 3L,
                    tags = listOf("회고"),
                    title = "프로젝트 회고",
                    contents = ExperienceContentsRequest(
                        type = ExperienceContentsType.FREE,
                        free = FreeExperienceContentsRequest(
                            content = "프로젝트 진행 과정에서 배운 점과 다음에 개선할 점을 자유롭게 정리했습니다.",
                        ),
                    ),
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.experienceId") { value(100) }
            jsonPath("$.result.title") { value("프로젝트 회고") }
            jsonPath("$.result.contents.type") { value("FREE") }
            jsonPath("$.result.contents.free.content") {
                value("프로젝트 진행 과정에서 배운 점과 다음에 개선할 점을 자유롭게 정리했습니다.")
            }
        }.andDo {
            handle(
                document(
                    "experience-create-free",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                    ),
                ),
            )
        }
    }

    "STAR 타입 경험 내용은 각 항목이 공백일 수 없다" {
        mockMvc.post("/v1/workspaces/{workspaceId}/experiences", "workspace-id") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                CreateExperienceRequest(
                    projectId = 3L,
                    title = "런칭 캠페인 메시지 A/B 테스트",
                    contents = ExperienceContentsRequest(
                        type = ExperienceContentsType.STAR,
                        star = StarExperienceContentsRequest(
                            situation = "신규 브랜드 런칭 직후 핵심 메시지에 대한 반응 데이터가 부족했습니다.",
                            task = "제한된 예산 안에서 전환 가능성이 높은 메시지를 빠르게 찾아야 했습니다.",
                            action = " ",
                            result = "혜택 강조형 메시지의 클릭률과 회원가입 전환율이 가장 높았습니다.",
                        ),
                    ),
                ),
            )
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.ok") { value(false) }
            jsonPath("$.error.code") { value(CommonErrorCode.E400_INVALID_ARGUMENTS.code) }
            jsonPath("$.error.details[0].field") { value("contents.star.action") }
            jsonPath("$.error.details[0].reason") { value("must not be blank") }
        }
    }

    "경험 내용 타입과 다른 payload가 함께 오면 실패한다" {
        mockMvc.post("/v1/workspaces/{workspaceId}/experiences", "workspace-id") {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                CreateExperienceRequest(
                    projectId = 3L,
                    title = "런칭 캠페인 메시지 A/B 테스트",
                    contents = ExperienceContentsRequest(
                        type = ExperienceContentsType.FREE,
                        star = StarExperienceContentsRequest(
                            situation = "상황",
                            task = "과제",
                            action = "행동",
                            result = "결과",
                        ),
                        free = FreeExperienceContentsRequest(
                            content = "자유 형식 내용",
                        ),
                    ),
                ),
            )
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.ok") { value(false) }
            jsonPath("$.error.code") { value(CommonErrorCode.E400_INVALID_ARGUMENTS.code) }
            jsonPath("$.error.details[0].field") { value("contents.validByType") }
            jsonPath("$.error.details[0].reason") {
                value("STAR contents require only star. FREE contents require only free.")
            }
        }
    }

    "경험을 수정한다" {
        mockMvc.patch("/v1/workspaces/{workspaceId}/experiences/{experienceId}", "workspace-id", 5L) {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                UpdateExperienceRequest(
                    title = "런칭 캠페인 메시지 실험 개선",
                    contents = ExperienceContentsRequest(
                        type = ExperienceContentsType.STAR,
                        star = StarExperienceContentsRequest(
                            situation = "런칭 캠페인 데이터가 누적되면서 메시지별 성과 차이가 명확해졌습니다.",
                            task = "성과가 높은 메시지를 중심으로 캠페인 구조를 재정리해야 했습니다.",
                            action = "전환율이 높은 메시지를 메인 카피로 반영하고 소재 그룹을 재구성했습니다.",
                            result = "주요 소재의 클릭률과 가입 전환율이 함께 개선되었습니다.",
                        ),
                    ),
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.experienceId") { value(5) }
            jsonPath("$.result.project.projectId") { value(3) }
            jsonPath("$.result.tags[0]") { value("브랜드런칭") }
            jsonPath("$.result.title") { value("런칭 캠페인 메시지 실험 개선") }
            jsonPath("$.result.contents.type") { value("STAR") }
            jsonPath("$.result.contents.star.result") {
                value("주요 소재의 클릭률과 가입 전환율이 함께 개선되었습니다.")
            }
        }.andDo {
            handle(
                document(
                    "experience-update",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                        parameterWithName("experienceId").description("경험 ID"),
                    ),
                    experienceUpdateRequestFields(),
                    experienceResponseFields(),
                    experienceErrorCodeSnippet(
                        ExperienceErrorCode.E404_EXPERIENCE_NOT_FOUND to
                            ExperienceErrorCode.E404_EXPERIENCE_NOT_FOUND.description,
                        ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND to
                            "변경할 경험 프로젝트를 찾을 수 없는 경우",
                    ),
                ),
            )
        }
    }

    "FREE 타입 경험을 수정한다" {
        mockMvc.patch("/v1/workspaces/{workspaceId}/experiences/{experienceId}", "workspace-id", 5L) {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
            contentType = MediaType.APPLICATION_JSON
            content = JsonUtils.toJson(
                UpdateExperienceRequest(
                    title = "프로젝트 회고 업데이트",
                    contents = ExperienceContentsRequest(
                        type = ExperienceContentsType.FREE,
                        free = FreeExperienceContentsRequest(
                            content = "프로젝트를 다시 돌아보며 의사결정 기준과 다음 실험 아이디어를 보완했습니다.",
                        ),
                    ),
                ),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result.experienceId") { value(5) }
            jsonPath("$.result.title") { value("프로젝트 회고 업데이트") }
            jsonPath("$.result.contents.type") { value("FREE") }
            jsonPath("$.result.contents.free.content") {
                value("프로젝트를 다시 돌아보며 의사결정 기준과 다음 실험 아이디어를 보완했습니다.")
            }
        }.andDo {
            handle(
                document(
                    "experience-update-free",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                        parameterWithName("experienceId").description("경험 ID"),
                    ),
                ),
            )
        }
    }

    "경험을 삭제한다" {
        mockMvc.delete("/v1/workspaces/{workspaceId}/experiences/{experienceId}", "workspace-id", 5L) {
            header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
        }.andExpect {
            status { isOk() }
            jsonPath("$.ok") { value(true) }
            jsonPath("$.result") { doesNotExist() }
        }.andDo {
            handle(
                document(
                    "experience-delete",
                    RestDocsUtils.getDocumentRequest(),
                    RestDocsUtils.getDocumentResponse(),
                    PageHeaderSnippet.pageHeaderSnippet(),
                    pathParameters(
                        parameterWithName("workspaceId").description("워크스페이스 ID"),
                        parameterWithName("experienceId").description("경험 ID"),
                    ),
                    responseFields(
                        fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                    ),
                    experienceErrorCodeSnippet(
                        ExperienceErrorCode.E404_EXPERIENCE_NOT_FOUND,
                    ),
                ),
            )
        }
    }

})

private fun experienceRequestFields() = requestFields(
    fieldWithPath("projectId").type(JsonFieldType.NUMBER).description("경험이 연결된 프로젝트 ID"),
    fieldWithPath("tags").type(JsonFieldType.ARRAY).description("경험 태그 목록")
        .attributes(RestDocsUtils.remarks("최대 10개")),
    fieldWithPath("title").type(JsonFieldType.STRING).description("경험 제목")
        .attributes(RestDocsUtils.remarks("공백 불가, 최대 150자")),
    fieldWithPath("contents").type(JsonFieldType.OBJECT).description("경험 상세 내용"),
    fieldWithPath("contents.type").type(JsonFieldType.STRING).description("경험 상세 내용 타입")
        .attributes(RestDocsUtils.remarks("STAR, FREE")),
    fieldWithPath("contents.star").type(JsonFieldType.OBJECT).description("STAR 형식의 상세 내용").optional(),
    fieldWithPath("contents.star.situation").type(JsonFieldType.STRING).description("STAR 형식의 상황").optional(),
    fieldWithPath("contents.star.task").type(JsonFieldType.STRING).description("STAR 형식의 과제").optional(),
    fieldWithPath("contents.star.action").type(JsonFieldType.STRING).description("STAR 형식의 행동").optional(),
    fieldWithPath("contents.star.result").type(JsonFieldType.STRING).description("STAR 형식의 결과").optional(),
    fieldWithPath("contents.free").type(JsonFieldType.OBJECT).description("FREE 형식의 상세 내용").optional(),
    fieldWithPath("contents.free.content").type(JsonFieldType.STRING).description("FREE 형식의 자유 내용").optional(),
)

private fun experienceUpdateRequestFields() = requestFields(
    fieldWithPath("projectId").type(JsonFieldType.NUMBER).description("변경할 프로젝트 ID").optional(),
    fieldWithPath("tags").type(JsonFieldType.ARRAY).description("변경할 경험 태그 목록").optional()
        .attributes(RestDocsUtils.remarks("값이 있으면 최대 10개")),
    fieldWithPath("title").type(JsonFieldType.STRING).description("변경할 경험 제목").optional()
        .attributes(RestDocsUtils.remarks("값이 있으면 공백 불가, 최대 150자")),
    fieldWithPath("contents").type(JsonFieldType.OBJECT).description("변경할 경험 상세 내용").optional(),
    fieldWithPath("contents.type").type(JsonFieldType.STRING).description("변경할 경험 상세 내용 타입").optional()
        .attributes(RestDocsUtils.remarks("STAR, FREE")),
    fieldWithPath("contents.star").type(JsonFieldType.OBJECT).description("변경할 STAR 형식의 상세 내용").optional(),
    fieldWithPath("contents.star.situation").type(JsonFieldType.STRING).description("변경할 STAR 형식의 상황").optional(),
    fieldWithPath("contents.star.task").type(JsonFieldType.STRING).description("변경할 STAR 형식의 과제").optional(),
    fieldWithPath("contents.star.action").type(JsonFieldType.STRING).description("변경할 STAR 형식의 행동").optional(),
    fieldWithPath("contents.star.result").type(JsonFieldType.STRING).description("변경할 STAR 형식의 결과").optional(),
    fieldWithPath("contents.free").type(JsonFieldType.OBJECT).description("변경할 FREE 형식의 상세 내용").optional(),
    fieldWithPath("contents.free.content").type(JsonFieldType.STRING).description("변경할 FREE 형식의 자유 내용").optional(),
)

private fun experienceResponseFields() = responseFields(
    fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
    fieldWithPath("result.experienceId").type(JsonFieldType.NUMBER).description("경험 ID"),
    fieldWithPath("result.project").type(JsonFieldType.OBJECT).description("경험이 연결된 프로젝트").optional(),
    fieldWithPath("result.project.projectId").type(JsonFieldType.NUMBER).description("프로젝트 ID").optional(),
    fieldWithPath("result.project.name").type(JsonFieldType.STRING).description("프로젝트 이름").optional(),
    fieldWithPath("result.project.summary").type(JsonFieldType.STRING).description("프로젝트 요약").optional(),
    fieldWithPath("result.project.period").type(JsonFieldType.OBJECT).description("프로젝트 진행 기간").optional(),
    fieldWithPath("result.project.period.startAt").type(JsonFieldType.STRING).description("프로젝트 시작일").optional(),
    fieldWithPath("result.project.period.endAt").type(JsonFieldType.STRING).description("프로젝트 종료일").optional(),
    fieldWithPath("result.project.role").type(JsonFieldType.STRING).description("프로젝트에서 맡은 역할").optional(),
    fieldWithPath("result.tags").type(JsonFieldType.ARRAY).description("경험 태그 목록"),
    fieldWithPath("result.title").type(JsonFieldType.STRING).description("경험 제목"),
    fieldWithPath("result.contents").type(JsonFieldType.OBJECT).description("경험 상세 내용"),
    fieldWithPath("result.contents.type").type(JsonFieldType.STRING).description("경험 상세 내용 타입"),
    fieldWithPath("result.contents.star").type(JsonFieldType.OBJECT).description("STAR 형식의 상세 내용").optional(),
    fieldWithPath("result.contents.star.situation").type(JsonFieldType.STRING).description("STAR 형식의 상황").optional(),
    fieldWithPath("result.contents.star.task").type(JsonFieldType.STRING).description("STAR 형식의 과제").optional(),
    fieldWithPath("result.contents.star.action").type(JsonFieldType.STRING).description("STAR 형식의 행동").optional(),
    fieldWithPath("result.contents.star.result").type(JsonFieldType.STRING).description("STAR 형식의 결과").optional(),
    fieldWithPath("result.contents.free").type(JsonFieldType.OBJECT).description("FREE 형식의 상세 내용").optional(),
    fieldWithPath("result.contents.free.content").type(JsonFieldType.STRING).description("FREE 형식의 자유 내용").optional(),
)

private fun experienceErrorCodeSnippet(
    vararg errorCodes: Pair<ErrorCode, String>,
) = ErrorCodeSnippet.errorCodeSnippet(
    WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED to WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED.description,
    WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND to WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND.description,
    *errorCodes,
)

private fun experienceErrorCodeSnippet(
    vararg errorCodes: ErrorCode,
) = experienceErrorCodeSnippet(
    *errorCodes.map { it to it.description }.toTypedArray(),
)

private fun experience(
    id: Long,
    projectId: Long,
    tags: List<String>,
    title: String,
    contents: ExperienceContents,
) = Experience(
    id = id,
    workspaceId = 1L,
    projectId = projectId,
    tags = tags,
    title = title,
    contents = contents,
    displayOrder = BigDecimal.ZERO,
    status = ExperienceStatus.ACTIVE,
)

private fun project(id: Long) = ExperienceProject(
    id = id,
    workspaceId = 1L,
    name = "신규 브랜드 런칭 캠페인",
    summary = "신규 서비스의 초기 인지도 확보 캠페인",
    period = null,
    role = "Growth Marketer",
    displayOrder = BigDecimal.ZERO,
    status = ExperienceProjectStatus.ACTIVE,
)
