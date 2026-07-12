package com.jobdori.core.domain.resume.service.command

import com.jobdori.core.domain.resume.ResumeSectionItemPayload
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.ResumeTemplate

data class ResumeSaveCommand(
    val targetJdId: Long?,
    val title: String,
    val template: ResumeTemplate,
    val status: ResumeStatus,
    val sections: List<ResumeSectionSaveCommand>,
)

data class ResumeSectionSaveCommand(
    val sectionId: Long?,
    val type: ResumeSectionType,
    val displayOrder: Double,
    val visible: Boolean,
    val items: List<ResumeSectionItemSaveCommand>,
)

data class ResumeSectionItemSaveCommand(
    val itemId: Long?,
    val payload: ResumeSectionItemPayload,
    val displayOrder: Double,
    val visible: Boolean,
)
