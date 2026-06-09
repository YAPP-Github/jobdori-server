package com.jobdori.api.application.sample.controller

import com.jobdori.api.application.sample.dto.response.SampleResponse
import com.jobdori.core.application.sample.service.GetSampleService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sample")
class GetSampleController(
    private val getSampleService: GetSampleService,
) {

    @GetMapping("/{id}")
    fun getSampleByName(@PathVariable id: Long): SampleResponse {
        val result = getSampleService.findById(sampleId = id)
        return SampleResponse(result.sampleId, result.name)
    }
}
