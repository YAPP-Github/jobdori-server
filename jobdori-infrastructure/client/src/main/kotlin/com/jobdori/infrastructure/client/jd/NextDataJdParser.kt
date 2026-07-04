package com.jobdori.infrastructure.client.jd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.jobdori.common.json.JsonUtils
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

/**
 * Next.js `__NEXT_DATA__`(원티드형 `props.pageProps.initialData`)에서 JD를 파싱한다.
 * 클라이언트 렌더링돼 정적 추출로 놓치는 우대사항·전형절차까지 확보한다. 구조가 다르면 null → 폴백.
 */
@Component
class NextDataJdParser {

    fun parse(url: String, html: ByteArray?): String? {
        if (html == null) return null
        val json = extractNextData(html, url) ?: return null
        val data = runCatching { JsonUtils.toObject(json, NextData::class.java) }
            .getOrNull()?.props?.pageProps?.initialData ?: return null
        return assemble(data)
    }

    private fun extractNextData(html: ByteArray, url: String): String? =
        Jsoup.parse(ByteArrayInputStream(html), null, url)
            .selectFirst("script#__NEXT_DATA__")
            ?.data()
            ?.takeIf { it.isNotBlank() }

    // 라벨은 JD_META_EXTRACTION 프롬프트가 7필드로 매핑하기 좋게 붙인다(자격요건→필요경험, 우대사항→우대경험, 채용전형→전형절차)
    private fun assemble(d: NextData.InitialData): String? {
        // JD 핵심 필드가 하나도 없으면 채용 공고가 아님(다른 Next.js 페이지) → 폴백
        if (d.position.isNullOrBlank() && d.mainTasks.isNullOrBlank() && d.requirements.isNullOrBlank()) return null

        val sections = buildList {
            section("포지션", d.position)
            section("기업명", d.company?.companyName)
            section("기업 소개", d.company?.companyDescription)
            section("포지션 상세", d.intro)
            section("주요업무", d.mainTasks)
            section("자격요건", d.requirements)
            section("우대사항", d.preferredPoints)
            section("채용 전형", d.hireRounds)
            section("복지 및 혜택", d.benefits)
        }
        return sections.joinToString("\n\n").ifBlank { null }
    }

    private fun MutableList<String>.section(label: String, value: String?) {
        if (!value.isNullOrBlank()) add("[$label]\n${value.trim()}")
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class NextData(val props: Props? = null) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Props(val pageProps: PageProps? = null)

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class PageProps(val initialData: InitialData? = null)

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class InitialData(
            val position: String? = null,
            val company: Company? = null,
            val intro: String? = null,
            @JsonProperty("main_tasks") val mainTasks: String? = null,
            val requirements: String? = null,
            @JsonProperty("preferred_points") val preferredPoints: String? = null,
            @JsonProperty("hire_rounds") val hireRounds: String? = null,
            val benefits: String? = null,
        )

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Company(
            @JsonProperty("company_name") val companyName: String? = null,
            @JsonProperty("company_description") val companyDescription: String? = null,
        )
    }
}
