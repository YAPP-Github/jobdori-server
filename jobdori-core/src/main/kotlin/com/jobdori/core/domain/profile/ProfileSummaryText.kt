package com.jobdori.core.domain.profile

import com.jobdori.common.model.Period

// 프로필 원본 정보(인적사항/학력/경력/어학/수상/자격증/스킬)를 AI 프롬프트 입력용 지원자 요약 텍스트로 재구성한다.
// 핵심역량 생성/지원 전략 생성 등 프로필을 프롬프트에 넣는 곳이 공유한다.
// 핵심역량(coreCompetency)은 AI가 이 요약으로 생성하는 파생 데이터라 입력에 넣지 않는다(자기참조/시점 불일치 방지). 연락처도 제외.
object ProfileSummaryText {

    fun of(detail: ProfileDetail): String {
        val p = detail.profile
        val s = detail.sections
        val blocks = mutableListOf<String>()

        p.name.nonBlank()?.let { blocks += "[이름]\n$it" }

        listBlock("학력", s.educations) {
            line(it.school, it.major, it.degree?.name, it.status?.name, period(it.period))
        }?.let { blocks += it }

        listBlock("경력", s.careers) {
            val who = listOfNotNull(it.company.nonBlank(), it.position.nonBlank()).joinToString(" / ")
            val head = line(who.nonBlank(), period(it.period))
            it.description.nonBlank()?.let { desc -> "$head: $desc" } ?: head
        }?.let { blocks += it }

        listBlock("어학", s.languageTests) {
            line(it.testName, it.score, it.acquiredAt?.toString())
        }?.let { blocks += it }

        listBlock("수상", s.awards) {
            line(it.title, it.organization, it.awardedAt?.toString())
        }?.let { blocks += it }

        listBlock("자격증", s.certifications) {
            line(it.name, it.issuer, it.acquiredAt?.toString())
        }?.let { blocks += it }

        s.skills
            .mapNotNull { sk -> sk.name.nonBlank()?.let { if (sk.level != null) "$it(${sk.level.name})" else it } }
            .takeIf { it.isNotEmpty() }
            ?.let { blocks += "[스킬]\n${it.joinToString(", ")}" }

        return blocks.joinToString("\n\n")
    }

    private fun <T> listBlock(label: String, items: List<T>, render: (T) -> String): String? {
        val lines = items.map(render).map(String::trim).filter(String::isNotBlank)
        return if (lines.isEmpty()) null else "[$label]\n" + lines.joinToString("\n") { "- $it" }
    }

    private fun line(vararg parts: String?): String =
        parts.mapNotNull { it.nonBlank() }.joinToString(" ")

    private fun period(period: Period?): String? {
        if (period?.startAt == null && period?.endAt == null) return null
        return "(${period.startAt ?: ""} - ${period.endAt ?: ""})"
    }

    private fun String?.nonBlank(): String? = this?.takeIf { it.isNotBlank() }
}
