package com.jobdori.core.application.experience

// 이력서 가져오기는 STAR 재구성 없이 원문 세부 내용을 보존한다.
data class ResumeImportExtractionResult(
    val personalInfo: ResumeImportPersonalInfo = ResumeImportPersonalInfo(),
    val education: List<ExtractedEducation> = emptyList(),
    val careers: List<ExtractedCareer> = emptyList(),
    val languageTests: List<ExtractedLanguageTest> = emptyList(),
    val awards: List<ExtractedAward> = emptyList(),
    val certifications: List<ExtractedCertification> = emptyList(),
    val skills: List<ExtractedSkill> = emptyList(),
    val projects: List<ResumeImportProject> = emptyList(),
)

data class ResumeImportPersonalInfo(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
)

data class ResumeImportProject(
    val name: String = "",
    val summary: String = "",
    val period: ExtractedPeriod = ExtractedPeriod(),
    val periodText: String = "",
    val role: String = "",
    val company: String = "",
    val experiences: List<ResumeImportExperience> = emptyList(),
)

data class ResumeImportExperience(
    val title: String = "",
    val period: ExtractedPeriod = ExtractedPeriod(),
    val periodText: String = "",
    val role: String = "",
    val content: String = "",
    val competencyTags: List<String> = emptyList(),
)
