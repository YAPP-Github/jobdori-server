package com.jobdori.core.application.jdinsight

import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import com.jobdori.core.domain.jdinsight.JdInsight
import com.jobdori.core.domain.jdinsight.repository.JdInsightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetJdInsightService(
    private val jdRepository: JdRepository,
    private val jdInsightRepository: JdInsightRepository,
    private val generateJdInsightService: GenerateJdInsightService,
) {

    // 최초 조회 시 생성·저장하고 이후에는 캐시된 인사이트를 재사용한다(lazy 캐시).
    @Transactional
    fun getOrGenerate(workspaceId: Long, jdPublicId: String): JdInsight {
        val jd = jdRepository.findByPublicIdAndWorkspaceId(jdPublicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($jdPublicId)입니다")
        return jdInsightRepository.findByJdId(jd.id)
            ?: jdInsightRepository.save(generateJdInsightService.generate(jd))
    }

}
