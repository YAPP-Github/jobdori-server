package com.jobdori.core.domain.resume.service

import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.repository.ResumeRepository
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import org.springframework.stereotype.Service

@Service
class ResumeCreator(
    private val resumeRepository: ResumeRepository,
) {

    fun create(
        workspaceId: Long,
        command: ResumeSaveCommand,
    ): ResumeDetail {
        return resumeRepository.createDetail(workspaceId = workspaceId, command = command)
    }

}
