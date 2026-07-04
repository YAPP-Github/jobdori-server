package com.jobdori.infrastructure.client.jd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.jobdori.common.json.JsonUtils
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

/**
 * schema.org `JobPosting` JSON-LD 파서(Google for Jobs 표준 — 당근·그린하우스·레버 등 다수 사이트).
 * `__NEXT_DATA__`가 없는 사이트를 커버한다. JobPosting이 없으면 null → 폴백.
 */
@Component
class JsonLdJdParser {

    fun parse(url: String, html: ByteArray?): String? {
        if (html == null) return null
        val doc = Jsoup.parse(ByteArrayInputStream(html), null, url)
        val jobPosting = doc.select("script[type=application/ld+json]")
            .asSequence()
            .flatMap { findJobPosting(it.data()) }
            .firstOrNull() ?: return null
        return assemble(jobPosting)
    }

    // ld+json은 단일 객체 / 배열 / {"@graph":[...]} 형태가 모두 가능 → 후보를 펼쳐 JobPosting을 찾는다
    private fun findJobPosting(json: String): Sequence<JobPosting> {
        val root = runCatching { JsonUtils.DEFAULT_JSON_MAPPER.readTree(json) }.getOrNull()
            ?: return emptySequence()
        val candidates = when {
            root.isArray -> root.toList()
            root.has("@graph") && root.get("@graph").isArray -> root.get("@graph").toList()
            else -> listOf(root)
        }
        // @type 문자열/배열 편차를 피하려고, title+description을 가진 노드를 JobPosting으로 간주
        return candidates.asSequence()
            .mapNotNull { node -> runCatching { JsonUtils.DEFAULT_JSON_MAPPER.treeToValue(node, JobPosting::class.java) }.getOrNull() }
            .filter { !it.title.isNullOrBlank() && !it.description.isNullOrBlank() }
    }

    private fun assemble(jp: JobPosting): String {
        val body = htmlToText(jp.description!!)
        return buildString {
            append("[포지션]\n").append(jp.title!!.trim())
            jp.hiringOrganization?.name?.takeIf { it.isNotBlank() }?.let { append("\n\n[기업명]\n").append(it.trim()) }
            append("\n\n").append(body)
        }
    }

    // JobPosting.description은 HTML → 블록 태그를 줄바꿈으로 바꿔 섹션 구분을 살린 뒤 태그 제거·엔티티 복원
    private fun htmlToText(descriptionHtml: String): String =
        descriptionHtml
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("(?i)</(p|li|h[1-6]|div|ul|ol|tr|section)>"), "\n")
            .replace(Regex("<[^>]+>"), " ")
            .let { Parser.unescapeEntities(it, false) }
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\s*\n\\s*"), "\n")
            .trim()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class JobPosting(
        val title: String? = null,
        val description: String? = null,   // HTML
        val hiringOrganization: Organization? = null,
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Organization(val name: String? = null)
    }
}
