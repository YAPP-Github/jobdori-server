package com.jobdori.core.domain.resume

data class ResumeSkillPayload(
    val name: String?,
    val level: String?,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.SKILL
}
