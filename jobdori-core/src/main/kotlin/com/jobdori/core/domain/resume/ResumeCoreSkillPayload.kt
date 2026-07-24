package com.jobdori.core.domain.resume

data class ResumeCoreSkillPayload(
    val content: String?,
    val isInitialItem: Boolean = true,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.CORE_SKILL
}
