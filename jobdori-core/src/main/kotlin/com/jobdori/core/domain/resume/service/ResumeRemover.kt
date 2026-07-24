package com.jobdori.core.domain.resume.service

import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.error.ResumeNotFoundException
import com.jobdori.core.domain.resume.repository.ResumeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResumeRemover(
    private val resumeRepository: ResumeRepository,
) {

    @Transactional
    fun remove(workspaceId: Long, resumeId: Long) {
        val resume = resumeRepository.findByIdAndWorkspaceId(
            id = resumeId,
            workspaceId = workspaceId,
        ) ?: throw ResumeNotFoundException("존재하지 않는 이력서입니다. [workspaceId=$workspaceId, resumeId=$resumeId]")

        resumeRepository.save(resume.copy(status = ResumeStatus.DELETED))
    }

}
