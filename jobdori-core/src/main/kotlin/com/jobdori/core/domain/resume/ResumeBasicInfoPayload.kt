package com.jobdori.core.domain.resume

data class ResumeBasicInfoPayload(
    val name: String,
    val email: String?,
    val phone: String?,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.BASIC_INFO
}
