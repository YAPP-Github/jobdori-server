package com.jobdori.core.domain.resume

sealed interface ResumeSectionItemPayload {
    val type: ResumeSectionType
}
