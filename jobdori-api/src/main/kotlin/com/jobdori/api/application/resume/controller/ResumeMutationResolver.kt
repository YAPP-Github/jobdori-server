package com.jobdori.api.application.resume.controller

import com.jobdori.api.application.resume.dto.request.SaveResumeRequest
import com.jobdori.api.application.resume.dto.response.ResumeResponse
import com.jobdori.api.application.resume.service.ResumeService
import com.jobdori.api.support.auth.UserId
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class ResumeMutationResolver(
    private val resumeService: ResumeService,
) {

    @MutationMapping
    fun createResume(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument input: SaveResumeRequest,
    ): ResumeResponse = resumeService.createResume(
        userId = userId,
        workspaceId = workspaceId,
        request = input,
    )
    @MutationMapping
    fun updateResume(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument resumeId: Long,
        @Argument input: SaveResumeRequest,
    ): ResumeResponse = resumeService.modifyResume(
        userId = userId,
        workspaceId = workspaceId,
        resumeId = resumeId,
        request = input,
    )

    @MutationMapping
    fun deleteResume(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument resumeId: Long,
    ): Boolean {
        resumeService.deleteResume(
            userId = userId,
            workspaceId = workspaceId,
            resumeId = resumeId,
        )
        return true
    }

}
