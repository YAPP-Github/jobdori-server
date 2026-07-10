package com.jobdori.api.application.jd.controller

import com.jobdori.api.application.jd.dto.request.JdRegisterRequest
import com.jobdori.api.application.jd.dto.response.JdInsightResponse
import com.jobdori.api.application.jd.dto.response.JdRegisterResponse
import com.jobdori.api.application.jd.dto.response.JdResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.jd.RegisterJdService
import com.jobdori.core.application.jdinsight.GetJdInsightService
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class JdResolver(
    private val registerJdService: RegisterJdService,
    private val getJdService: GetJdService,
    private val getJdInsightService: GetJdInsightService,
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
) {

    // 크롤·추출 실패(JdCrawlException/AiException)는 GraphQLExceptionAdvice가 매핑
    @MutationMapping
    fun registerJd(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Valid @Argument request: JdRegisterRequest,
    ): JdRegisterResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        val result = if (!request.sourceUrl.isNullOrBlank()) {
            registerJdService.registerByUrl(workspace.id, request.sourceUrl)
        } else {
            registerJdService.registerByText(workspace.id, request.body!!)
        }
        return JdRegisterResponse.from(result)
    }

    @QueryMapping
    fun jd(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument id: String,
    ): JdResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        return JdResponse.from(getJdService.getJd(workspace.id, id))
    }

    @QueryMapping
    fun jds(
        @UserId userId: Long,
        @Argument workspaceId: String,
    ): List<JdResponse> {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        return getJdService.getJds(workspace.id).map { JdResponse.from(it) }
    }

    // 최초 조회 시 AI로 생성·저장하고 이후엔 캐시 반환. 생성 실패(AiException)는 GraphQLExceptionAdvice가 매핑
    @QueryMapping
    fun jdInsight(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument jdId: String,
    ): JdInsightResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        return JdInsightResponse.from(getJdInsightService.getOrGenerate(workspace.id, jdId))
    }

}
