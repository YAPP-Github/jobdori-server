package com.jobdori.core.domain.resume

data class ResumeCoreSkillPayload(
    val content: String,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.CORE_SKILL
}
