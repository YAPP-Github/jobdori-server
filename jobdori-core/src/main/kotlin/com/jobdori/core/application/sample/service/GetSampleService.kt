package com.jobdori.core.application.sample.service

import com.jobdori.core.application.sample.service.result.SampleResult

interface GetSampleService {
    fun findById(sampleId: Long): SampleResult
}
