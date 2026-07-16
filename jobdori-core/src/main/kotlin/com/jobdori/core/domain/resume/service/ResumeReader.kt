package com.jobdori.core.domain.resume.service

import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.error.ResumeNotFoundException
import com.jobdori.core.domain.resume.repository.ResumeRepository
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.model.SliceResult
import org.springframework.stereotype.Service

@Service
class ResumeReader(
    private val resumeRepository: ResumeRepository,
) {

    fun getResumes(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
        cursor: String?,
        size: Int,
    ): SliceResult<Resume> {
        val cursorId = cursor?.let {
            val parsedCursor = it.toLongOrNull()
            if (parsedCursor == null || parsedCursor < 0) {
                throw InvalidArgumentsException("유효하지 않은 커서 값입니다: $it")
            }
            parsedCursor
        }

        val resumes = resumeRepository.findAllByWorkspaceIdAndStatuses(
            workspaceId = workspaceId,
            statuses = statuses,
            cursorId = cursorId,
            size = size + 1,
        )
        val page = resumes.take(size)

        return SliceResult(
            items = page,
            nextCursor = if (resumes.size > size) page.lastOrNull()?.id?.toString() else null,
        )
    }

    fun countResumes(workspaceId: Long, statuses: Collection<ResumeStatus>): Map<ResumeStatus, Long> {
        if (statuses.isEmpty()) {
            return emptyMap()
        }

        val counts = resumeRepository.countByWorkspaceIdAndStatuses(
            workspaceId = workspaceId,
            statuses = statuses,
        )

        return statuses.distinct().associateWith { counts[it] ?: 0L }
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
