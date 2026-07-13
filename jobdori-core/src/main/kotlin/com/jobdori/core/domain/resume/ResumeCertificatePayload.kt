package com.jobdori.core.domain.resume

import java.time.LocalDate

data class ResumeCertificatePayload(
    val name: String,
    val organization: String?,
    val acquiredAt: LocalDate?,
) : ResumeSectionItemPayload {
    override val type = ResumeSectionType.CERTIFICATE
}
