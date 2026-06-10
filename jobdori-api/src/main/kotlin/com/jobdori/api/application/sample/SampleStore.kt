package com.jobdori.api.application.sample

import com.jobdori.api.application.sample.dto.response.SampleResponse

internal object SampleStore {

    private val samples = mutableListOf(
        SampleResponse(sampleId = 1L, name = "1번 샘플"),
        SampleResponse(sampleId = 2L, name = "2번 샘플"),
    )
    private var nextSampleId = 3L

    fun findAll(): List<SampleResponse> {
        return samples.toList()
    }

    fun findById(sampleId: Long): SampleResponse? {
        return samples.find { it.sampleId == sampleId }
    }

    fun create(name: String): SampleResponse {
        val sample = SampleResponse(
            sampleId = nextSampleId++,
            name = name,
        )

        samples.add(sample)

        return sample
    }

    fun update(sampleId: Long, name: String): SampleResponse? {
        val index = samples.indexOfFirst { it.sampleId == sampleId }
        if (index < 0) {
            return null
        }

        val sample = samples[index].copy(name = name)
        samples[index] = sample

        return sample
    }

    fun delete(sampleId: Long): Boolean {
        return samples.removeIf { it.sampleId == sampleId }
    }

    fun reset(vararg values: SampleResponse) {
        samples.clear()
        samples.addAll(values)
        nextSampleId = (samples.maxOfOrNull { it.sampleId } ?: 0L) + 1L
    }

}
