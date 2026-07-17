package com.jobdori.api.application.jd.controller

import com.jobdori.api.application.jd.dto.request.JdRegisterRequest
import com.jobdori.api.application.jd.dto.response.JdRegisterResponse
import com.jobdori.api.application.jd.dto.response.JdResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.jd.CompleteJdService
import com.jobdori.core.application.jd.DeleteJdService
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.jd.RegisterJdService
import com.jobdori.core.domain.jd.JdSortType
import com.jobdori.core.domain.jd.JdStatus
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class JdResolver(
    private val registerJdService: RegisterJdService,
    private val deleteJdService: DeleteJdService,
    private val completeJdService: CompleteJdService,
    private val getJdService: GetJdService,
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
) {

    // 크롤/추출 실패(JdCrawlException/AiException)는 GraphQLExceptionAdvice가 매핑
    @MutationMapping
    fun registerJd(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Valid @Argument request: JdRegisterRequest,
    ): JdRegisterResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        // body 우선: 다중 공고 후보를 body로 재등록할 때 sourceUrl은 출처 메타로만 저장
        val result = if (!request.body.isNullOrBlank()) {
            registerJdService.registerByText(workspace.id, request.body, request.sourceUrl)
        } else {
            registerJdService.registerByUrl(workspace.id, request.sourceUrl!!)
        }
        return JdRegisterResponse.from(result)
    }

    @MutationMapping
    fun deleteJd(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument id: String,
    ): Boolean {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        deleteJdService.deleteJd(workspace.id, id)
        return true
    }

    // 이력서 생성 완료 시 호출 -> JD를 COMPLETED로 전환(AR0001 진행 중 -> 완료). 향후 Resume 완료 플로우에서 연동.
    @MutationMapping
    fun markJdCompleted(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument id: String,
    ): JdResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        return JdResponse.from(completeJdService.markCompleted(workspace.id, id))
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
        @Argument sort: JdSortType,
        @Argument status: JdStatus?,
    ): List<JdResponse> {
        val workspace = workspaceAccessValidationService.validateAccessible(workspaceId, userId)
        return getJdService.getJds(workspace.id, sort, status).map { JdResponse.from(it) }
    }

}
