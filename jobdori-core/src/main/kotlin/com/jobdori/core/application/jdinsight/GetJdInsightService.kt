package com.jobdori.core.application.jdinsight

import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import com.jobdori.core.domain.jdinsight.JdInsight
import com.jobdori.core.domain.jdinsight.repository.JdInsightRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class GetJdInsightService(
    private val jdRepository: JdRepository,
    private val jdInsightRepository: JdInsightRepository,
    private val generateJdInsightService: GenerateJdInsightService,
) {

    fun getOrGenerate(workspaceId: Long, jdPublicId: String): JdInsight {
        val jd = jdRepository.findByPublicIdAndWorkspaceId(jdPublicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($jdPublicId)입니다")
        jdInsightRepository.findByJdId(jd.id)?.let { return it }
        return try {
            jdInsightRepository.save(generateJdInsightService.generate(jd))
        } catch (e: DataIntegrityViolationException) {
            jdInsightRepository.findByJdId(jd.id) ?: throw e
        }
    }

}
