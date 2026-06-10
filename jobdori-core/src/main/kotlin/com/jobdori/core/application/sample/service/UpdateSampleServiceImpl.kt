package com.jobdori.core.application.sample.service

import com.jobdori.core.application.sample.service.command.UpdateSampleCommand
import com.jobdori.core.application.sample.service.result.SampleResult
import com.jobdori.core.domain.sample.error.SampleNotFoundException
import com.jobdori.core.domain.sample.repository.SampleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateSampleServiceImpl(
    private val sampleRepository: SampleRepository,
) : UpdateSampleService {

    @Transactional
    override fun update(command: UpdateSampleCommand): SampleResult {
        val sample = sampleRepository.findById(command.sampleId)
            ?: throw SampleNotFoundException("등록되지 않은 샘플(${command.sampleId}) 입니다")

        // 도메인 모델이 이름 변경 규칙(SampleName VO 검증)을 책임진다.
        val renamed = sample.rename(command.name)

        val saved = sampleRepository.save(renamed)
        return SampleResult(saved.id, saved.name.value)
    }
}
