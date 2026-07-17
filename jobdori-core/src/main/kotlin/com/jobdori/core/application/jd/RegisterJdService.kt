package com.jobdori.core.application.jd

import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.ai.jd.ExtractJdMetaService
import com.jobdori.core.application.ai.jd.SplitJdPostingsService
import com.jobdori.core.application.ai.jd.result.JdMetaResult
import com.jobdori.core.application.jd.client.JdCrawlerClient
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdPolicy
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service

@Service
class RegisterJdService(
    private val crawler: JdCrawlerClient,
    private val splitter: SplitJdPostingsService,
    private val extractJdMetaService: ExtractJdMetaService,
    private val jdRepository: JdRepository,
) {
    // 크롤 실패 시 JdCrawlException 전파 -> API가 422로 붙여넣기 유도
    fun registerByUrl(workspaceId: Long, url: String): JdRegisterResult =
        register(workspaceId, sourceUrl = url, body = crawler.fetchBody(url))

    fun registerByText(workspaceId: Long, body: String, sourceUrl: String? = null): JdRegisterResult =
        register(workspaceId, sourceUrl = sourceUrl, body = body)

    private fun register(workspaceId: Long, sourceUrl: String?, body: String): JdRegisterResult {
        if (body.length !in JdPolicy.MIN_JD_BODY_LENGTH..JdPolicy.MAX_JD_LENGTH) {
            throw InvalidArgumentsException(
                "JD 본문은 ${JdPolicy.MIN_JD_BODY_LENGTH}자 이상 ${JdPolicy.MAX_JD_LENGTH}자 이하여야 합니다",
            )
        }
        val postings = splitter.split(body)   // 최소 1건 보장
        if (postings.size > 1) return JdRegisterResult.MultiplePostings(postings)

        val singleBody = postings.first().body
        val meta = extractJdMetaService.extractFromBody(singleBody)
        return JdRegisterResult.Registered(jdRepository.save(buildJd(workspaceId, sourceUrl, singleBody, meta)))
    }

    private fun buildJd(workspaceId: Long, sourceUrl: String?, sourceBody: String, meta: JdMetaResult): Jd = Jd.newInstance(
        workspaceId = workspaceId,
        sourceUrl = sourceUrl,
        sourceBody = sourceBody,
        companyName = meta.companyName,
        positionTitle = meta.positionTitle,
        companyIntro = meta.companyIntro,
        responsibilities = meta.responsibilities,
        requiredExperiences = meta.requiredExperiences,
        preferredExperiences = meta.preferredExperiences,
        hiringProcess = meta.hiringProcess,
        coreCompetencies = meta.coreCompetencies,
        keyPoints = meta.keyPoints,
        strategy = meta.strategy,
    )
}
