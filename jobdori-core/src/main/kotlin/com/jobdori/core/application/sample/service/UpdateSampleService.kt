package com.jobdori.core.application.sample.service

import com.jobdori.core.application.sample.service.command.UpdateSampleCommand
import com.jobdori.core.application.sample.service.result.SampleResult

interface UpdateSampleService {
    fun update(command: UpdateSampleCommand): SampleResult
}
