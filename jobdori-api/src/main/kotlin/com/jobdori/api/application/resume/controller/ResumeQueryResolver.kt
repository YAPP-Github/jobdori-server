package com.jobdori.api.application.resume.controller

import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.api.application.resume.dto.response.ResumeResponse
import com.jobdori.api.application.resume.dto.response.ResumeStatusCountResponse
import com.jobdori.api.application.resume.dto.response.ResumeSummaryResponse
import com.jobdori.api.application.resume.service.ResumeService
import com.jobdori.api.support.auth.UserId
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ResumeQueryResolver(
    private val resumeService: ResumeService,
) {

    @QueryMapping
    fun resumes(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument statuses: List<ResumeStatusType>?,
    ): List<ResumeSummaryResponse> = resumeService.getResumes(
        userId = userId,
        workspaceId = workspaceId,
        statuses = statuses,
    )

    @QueryMapping
    fun resumeCounts(
        @UserId userId: Long,
        @Argument workspaceId: String,
    ): List<ResumeStatusCountResponse> = resumeService.countResumes(
        userId = userId,
        workspaceId = workspaceId,
    )

    @QueryMapping
    fun resume(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument resumeId: Long,
        env: DataFetchingEnvironment,
    ): ResumeResponse = resumeService.getResume(
        userId = userId,
        workspaceId = workspaceId,
        resumeId = resumeId,
        includeSections = env.selectionSet.contains("sections"),
        includeSectionItems = env.selectionSet.contains("sections/items"),
    )

}
