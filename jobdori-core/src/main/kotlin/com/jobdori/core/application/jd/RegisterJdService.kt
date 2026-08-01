package com.jobdori.core.application.jd

import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.ai.jd.ExtractJdMetaService
import com.jobdori.core.application.ai.jd.ExtractJdStrategyService
import com.jobdori.core.application.ai.jd.SplitJdPostingsService
import com.jobdori.core.application.ai.jd.result.JdMetaResult
import com.jobdori.core.application.credit.CreditService
import com.jobdori.core.application.jd.client.JdCrawlerClient
import com.jobdori.core.domain.credit.CreditFeature
import com.jobdori.core.domain.experience.error.ExperienceRequiredException
import com.jobdori.core.domain.experience.service.ExperienceReader
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
    private val creditService: CreditService,
    private val extractJdMetaService: ExtractJdMetaService,
    private val extractJdStrategyService: ExtractJdStrategyService,
    private val profileReader: ProfileReader,
    private val experienceReader: ExperienceReader,
    private val jdRepository: JdRepository,
) {
    // 크롤 실패 시 JdCrawlException 전파 -> API가 422로 붙여넣기 유도
    fun registerByUrl(workspaceId: Long, userId: Long, url: String): JdRegisterResult {
        validateActiveExperienceExists(workspaceId)
        return register(workspaceId, userId, sourceUrl = url, body = crawler.fetchBody(url))
    }

    fun registerByText(workspaceId: Long, userId: Long, body: String, sourceUrl: String? = null): JdRegisterResult {
        validateActiveExperienceExists(workspaceId)
        return register(workspaceId, userId, sourceUrl = sourceUrl, body = body)
    }

    private fun validateActiveExperienceExists(workspaceId: Long) {
        if (experienceReader.findAllActive(workspaceId).isEmpty()) {
            throw ExperienceRequiredException()
        }
    }

    private fun register(workspaceId: Long, userId: Long, sourceUrl: String?, body: String): JdRegisterResult {
        if (body.length !in JdPolicy.MIN_JD_BODY_LENGTH..JdPolicy.MAX_JD_LENGTH) {
            throw InvalidArgumentsException(
                "JD 본문은 ${JdPolicy.MIN_JD_BODY_LENGTH}자 이상 ${JdPolicy.MAX_JD_LENGTH}자 이하여야 합니다",
            )
        }
        val postings = splitter.split(body)   // 최소 1건 보장
        if (postings.size > 1) return JdRegisterResult.MultiplePostings(postings)

        // 후보만 돌려준 시점에는 사용자가 JD를 받지 못했으므로, 단일 공고로 확정된 뒤에 차감한다.
        // 후보를 body로 재등록할 때 1회만 차감되고, 잔여 0이면 여기서 끊겨 아래 추출 AI가 돌지 않는다.
        creditService.consume(userId, CreditFeature.JD_ANALYSIS)
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
