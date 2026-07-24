package com.jobdori.core.domain.resume.service

import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.error.ResumeNotFoundException
import com.jobdori.core.domain.resume.repository.ResumeRepository
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import org.springframework.stereotype.Service

@Service
class ResumeModifier(
    private val resumeRepository: ResumeRepository,
) {

    fun modifyDetail(
        workspaceId: Long,
        resumeId: Long,
        command: ResumeSaveCommand,
    ): ResumeDetail {
        return resumeRepository.modifyDetail(
            id = resumeId,
            workspaceId = workspaceId,
            command = command,
        ) ?: throw ResumeNotFoundException(
            "존재하지 않는 이력서입니다. [workspaceId=$workspaceId, resumeId=$resumeId]",
        )
    }

    fun markCoreCompetencyGenerated(workspaceId: Long, resumeId: Long): Boolean {
        return resumeRepository.markCoreCompetencyGenerated(
            id = resumeId,
            workspaceId = workspaceId,
        )
    }

    fun resetCoreCompetencyGenerated(workspaceId: Long, resumeId: Long) {
        resumeRepository.resetCoreCompetencyGenerated(
            id = resumeId,
            workspaceId = workspaceId,
        )
    }

}
