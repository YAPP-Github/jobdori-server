package com.jobdori.api.application.resume.dto

import com.jobdori.core.domain.resume.ResumeStatus

enum class ResumeStatusType {

    COMPLETED,
    DRAFT,
    ;

    fun toDomain() = when (this) {
        COMPLETED -> ResumeStatus.COMPLETED
        DRAFT -> ResumeStatus.DRAFT
    }

    companion object {
        fun from(status: ResumeStatus): ResumeStatusType {
            return when (status) {
                ResumeStatus.COMPLETED -> COMPLETED
                ResumeStatus.DRAFT -> DRAFT
                ResumeStatus.DELETED -> error("DELETED 상태는 API 응답으로 노출할 수 없습니다.")
            }
        }
    }

}
