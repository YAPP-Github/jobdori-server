package com.jobdori.core.domain.resume.service

import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.error.ResumeNotFoundException
import com.jobdori.core.domain.resume.repository.ResumeRepository
import org.springframework.stereotype.Service

@Service
class ResumeReader(
    private val resumeRepository: ResumeRepository,
) {

    fun getResumes(workspaceId: Long, statuses: Collection<ResumeStatus>): List<Resume> {
        return resumeRepository.findAllByWorkspaceIdAndStatuses(
            workspaceId = workspaceId,
            statuses = statuses,
        )
    }

    fun getResume(workspaceId: Long, resumeId: Long): Resume {
        return resumeRepository.findByIdAndWorkspaceId(
            id = resumeId,
            workspaceId = workspaceId,
        ) ?: throw ResumeNotFoundException(
            "존재하지 않는 이력서입니다. [workspaceId=$workspaceId, resumeId=$resumeId]",
        )
    }

    fun getSections(workspaceId: Long, resumeId: Long): ResumeDetail {
        return resumeRepository.findSectionsByIdAndWorkspaceId(
            id = resumeId,
            workspaceId = workspaceId,
        ) ?: throw ResumeNotFoundException(
            "존재하지 않는 이력서입니다. [workspaceId=$workspaceId, resumeId=$resumeId]",
        )
    }

    fun getDetail(workspaceId: Long, resumeId: Long): ResumeDetail {
        return resumeRepository.findDetailByIdAndWorkspaceId(
            id = resumeId,
            workspaceId = workspaceId,
        ) ?: throw ResumeNotFoundException(
            "존재하지 않는 이력서입니다. [workspaceId=$workspaceId, resumeId=$resumeId]",
        )
    }

}
