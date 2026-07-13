package com.jobdori.infrastructure.persistence.domain.resume.repository

import com.jobdori.core.domain.resume.ResumeStatus

interface ResumeCustomRepository {

    fun countByWorkspaceIdAndStatusIn(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
    ): Map<ResumeStatus, Long>

}
