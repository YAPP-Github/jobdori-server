package com.jobdori.core.application.jd

import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.ai.jd.ExtractJdMetaService
import com.jobdori.core.application.ai.jd.SplitJdPostingsService
import com.jobdori.core.application.ai.jd.result.JdMetaResult
import com.jobdori.core.application.jd.client.JdCrawlerClient
import com.jobdori.core.application.jdinsight.GenerateJdInsightService
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdPolicy
import org.springframework.stereotype.Service

// 비로그인 게스트용 JD 분석. 저장하지 않고(워크스페이스·영속화 없음) 추출·인사이트 결과만 반환한다.
@Service
class AnalyzeGuestJdService(
    private val crawler: JdCrawlerClient,
    private val splitter: SplitJdPostingsService,
    private val extractJdMetaService: ExtractJdMetaService,
    private val generateJdInsightService: GenerateJdInsightService,
) {
    fun analyzeByUrl(url: String): GuestJdAnalysisResult =
        analyze(sourceUrl = url, body = crawler.fetchBody(url))

    fun analyzeByText(body: String): GuestJdAnalysisResult =
        analyze(sourceUrl = null, body = body)

    private fun analyze(sourceUrl: String?, body: String): GuestJdAnalysisResult {
        if (body.length !in JdPolicy.MIN_JD_BODY_LENGTH..JdPolicy.MAX_JD_LENGTH) {
            throw InvalidArgumentsException(
                "JD 본문은 ${JdPolicy.MIN_JD_BODY_LENGTH}자 이상 ${JdPolicy.MAX_JD_LENGTH}자 이하여야 합니다",
            )
        }
        val postings = splitter.split(body)   // 최소 1건 보장
        if (postings.size > 1) return GuestJdAnalysisResult.MultiplePostings(postings)

        val singleBody = postings.first().body
        val jd = buildTransientJd(sourceUrl, singleBody, extractJdMetaService.extractFromBody(singleBody))
        return GuestJdAnalysisResult.Analyzed(jd, generateJdInsightService.generate(jd))
    }

    // workspaceId 0L의 미저장 Jd. 인사이트 생성 입력으로만 쓰이고 DB에 들어가지 않는다.
    private fun buildTransientJd(sourceUrl: String?, sourceBody: String, meta: JdMetaResult): Jd = Jd.newInstance(
        workspaceId = 0L,
        sourceUrl = sourceUrl,
        sourceBody = sourceBody,
        companyName = meta.companyName,
        positionTitle = meta.positionTitle,
        companyIntro = meta.companyIntro,
        responsibilities = meta.responsibilities,
        requiredExperiences = meta.requiredExperiences,
        preferredExperiences = meta.preferredExperiences,
        hiringProcess = meta.hiringProcess,
    )
}
