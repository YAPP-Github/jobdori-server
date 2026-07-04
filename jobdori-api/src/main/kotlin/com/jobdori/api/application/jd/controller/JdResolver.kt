package com.jobdori.api.application.jd.controller

import com.jobdori.api.application.jd.dto.request.JdRegisterRequest
import com.jobdori.api.application.jd.dto.response.JdRegisterResponse
import com.jobdori.api.application.jd.dto.response.JdResponse
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.jd.RegisterJdService
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class JdResolver(
    private val registerJdService: RegisterJdService,
    private val getJdService: GetJdService,
) {

    // 크롤·추출 실패(JdCrawlException/AiException)는 GraphQLExceptionAdvice가 매핑
    @MutationMapping
    fun registerJd(
        @UserId userId: Long,
        @Valid @Argument request: JdRegisterRequest,
    ): JdRegisterResponse {
        val result = if (!request.sourceUrl.isNullOrBlank()) {
            registerJdService.registerByUrl(userId, request.sourceUrl)
        } else {
            registerJdService.registerByText(userId, request.body!!)
        }
        return JdRegisterResponse.from(result)
    }

    @QueryMapping
    fun jd(
        @UserId userId: Long,
        @Argument id: String,
    ): JdResponse = JdResponse.from(getJdService.getJd(userId, id))

    @QueryMapping
    fun jds(
        @UserId userId: Long,
    ): List<JdResponse> = getJdService.getMine(userId).map { JdResponse.from(it) }

}
