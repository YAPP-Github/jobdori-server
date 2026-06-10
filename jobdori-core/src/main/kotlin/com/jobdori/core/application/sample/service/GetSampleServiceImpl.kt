package com.jobdori.core.application.sample.service

import com.jobdori.core.domain.sample.repository.SampleRepository
import com.jobdori.core.application.sample.service.result.SampleResult
import com.jobdori.core.domain.sample.error.SampleNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetSampleServiceImpl(
    private val sampleRepository: SampleRepository
): GetSampleService {

    @Transactional(readOnly = true)
    override fun findById(sampleId: Long): SampleResult {
        val sample = sampleRepository.findById(sampleId)
            ?: throw SampleNotFoundException("등록되지 않은 샘플($sampleId) 입니다")
        return SampleResult(sample.id, sample.name.value)
    }
}
