package com.jobdori.core.domain.resume

data class ResumeDetail(
    val resume: Resume,
    val sections: List<ResumeDetailSection>,
)

data class ResumeDetailSection(
    val section: ResumeSection,
    val items: List<ResumeSectionItem>,
)
