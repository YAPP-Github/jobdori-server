package com.jobdori.core.application.jd

import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.ai.jd.ExtractJdMetaService
import com.jobdori.core.application.ai.jd.ExtractJdStrategyService
import com.jobdori.core.application.ai.jd.SplitJdPostingsService
import com.jobdori.core.application.ai.jd.result.JdMetaResult
import com.jobdori.core.application.jd.client.JdCrawlerClient
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdPolicy
import com.jobdori.core.domain.jd.error.JdCrawlErrorCode
import com.jobdori.core.domain.jd.error.JdCrawlException
import com.jobdori.core.domain.jd.repository.JdRepository
import com.jobdori.core.domain.profile.service.ProfileReader
import org.springframework.stereotype.Service

@Service
class RegisterJdService(
    private val crawler: JdCrawlerClient,
    private val splitter: SplitJdPostingsService,
    private val extractJdMetaService: ExtractJdMetaService,
    private val extractJdStrategyService: ExtractJdStrategyService,
    private val profileReader: ProfileReader,
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
        // JD가 아닌 URL/본문(예: 검색 페이지)이면 저장하지 않아 이후 추천까지 차단한다.
        if (!meta.isJobPosting || meta.hasNoJdSubstance()) {
            throw JdCrawlException(
                "채용 공고로 인식되지 않는 내용입니다.",
                JdCrawlErrorCode.E422_JD_NOT_A_POSTING,
            )
        }
        val jd = buildJd(workspaceId, sourceUrl, singleBody, meta)
        val profile = profileReader.getDetail(profileReader.getOrCreateProfile(workspaceId))
        val strategy = extractJdStrategyService.generate(jd, profile)
        return JdRegisterResult.Registered(jdRepository.save(jd.copy(strategy = strategy)))
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
        strategy = "",
    )
}
