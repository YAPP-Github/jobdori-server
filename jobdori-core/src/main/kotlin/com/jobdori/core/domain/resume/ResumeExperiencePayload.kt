package com.jobdori.core.domain.resume

import com.jobdori.common.model.Period

data class ResumeExperiencePayload(
    val name: String?,
    val role: String?,
    val period: Period?,
    val contents: String?,
    val experienceQuestionRoomId: String? = null,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.EXPERIENCE
}
