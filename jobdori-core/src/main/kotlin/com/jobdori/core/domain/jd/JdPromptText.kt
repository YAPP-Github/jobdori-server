package com.jobdori.core.domain.jd

// JD 원문은 저장하지 않으므로 구조화 메타를 AI 프롬프트 입력용 텍스트로 재구성한다.
// JD 인사이트·경험 추천 등 JD를 프롬프트에 넣는 모든 곳이 공유한다.
object JdPromptText {

    fun of(jd: Jd): String = buildString {
        appendLine("[기업명] ${jd.companyName}")
        appendLine("[포지션] ${jd.positionTitle}")
        appendLine("[기업/팀 소개] ${jd.companyIntro}")
        appendSection("업무 내용", jd.responsibilities)
        appendSection("필요 경험", jd.requiredExperiences)
        appendSection("우대 경험", jd.preferredExperiences)
        appendSection("전형 절차", jd.hiringProcess)
    }.trim()

    private fun StringBuilder.appendSection(label: String, items: List<String>) {
        if (items.isEmpty()) return
        appendLine("[$label]")
        items.forEach { appendLine("- $it") }
    }
}
