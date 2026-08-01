package com.jobdori.core.domain.resume.error

import com.jobdori.common.error.BaseException

data class ResumeSectionItemRequiredException(
    override val message: String = ResumeErrorCode.E400_RESUME_SECTION_ITEM_REQUIRED.message,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ResumeErrorCode.E400_RESUME_SECTION_ITEM_REQUIRED,
    cause = cause,
)
