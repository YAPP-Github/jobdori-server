package com.jobdori.api.application.health

import com.jobdori.api.ApiTest
import com.jobdori.api.DocsTest
import com.jobdori.api.libs.ErrorCodeSnippet.Companion.errorCodeSnippet
import com.jobdori.api.libs.PageHeaderSnippet.Companion.pageHeaderSnippet
import com.jobdori.api.libs.RestDocsUtils.getDocumentRequest
import com.jobdori.common.error.CommonErrorCode
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.availability.ReadinessState
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@DocsTest
@ApiTest(HealthCheckApi::class)
internal class HealthCheckApiDocsTest(
    private val mockMvc: MockMvc,
    @MockkBean
    private val applicationAvailability: ApplicationAvailability,
) : StringSpec({

    "Readiness Health Check API" {
        // given
        every { applicationAvailability.readinessState } returns ReadinessState.ACCEPTING_TRAFFIC

        // when & then
        mockMvc.get("/health/readiness")
            .andExpect {
                status { isOk() }

                jsonPath("$.ok") { value(true) }
            }
            .andDo {
                handle(
                    document(
                        "health-check",
                        getDocumentRequest(),
                        pageHeaderSnippet(),
                        errorCodeSnippet(
                            CommonErrorCode.E503_SERVICE_UNAVAILABLE to "서비스 상태가 아닌 경우 발생합니다",
                        ),
                        responseFields(
                            fieldWithPath("ok").type(JsonFieldType.BOOLEAN).description("API 처리 성공 여부"),
                        ),
                    ),
                )
            }
    }

})
