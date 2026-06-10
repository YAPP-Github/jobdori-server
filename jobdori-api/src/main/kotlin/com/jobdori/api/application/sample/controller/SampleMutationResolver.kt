package com.jobdori.api.application.sample.controller

import com.jobdori.api.application.sample.SampleStore
import com.jobdori.api.application.sample.dto.request.SampleCreateRequest
import com.jobdori.api.application.sample.dto.request.SampleDeleteRequest
import com.jobdori.api.application.sample.dto.request.SampleUpdateRequest
import com.jobdori.api.application.sample.dto.response.SampleResponse
import com.jobdori.core.application.sample.service.UpdateSampleService
import com.jobdori.core.application.sample.service.command.UpdateSampleCommand
import com.jobdori.core.domain.sample.error.SampleNotFoundException
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class SampleMutationResolver(
    private val updateSampleService: UpdateSampleService,
) {

    @MutationMapping
    fun createSample(
        @Argument @Valid request: SampleCreateRequest,
    ): SampleResponse {
        return SampleStore.create(name = request.name)
    }

    @MutationMapping
    fun updateSample(
        @Argument @Valid request: SampleUpdateRequest,
    ): SampleResponse {
        val result = updateSampleService.update(
            UpdateSampleCommand(
                sampleId = request.sampleId,
                name = request.name,
            )
        )
        return SampleResponse(
            sampleId = result.sampleId,
            name = result.name,
        )
    }

    @MutationMapping
    fun deleteSample(
        @Argument @Valid request: SampleDeleteRequest,
    ) {
        if (!SampleStore.delete(request.sampleId)) {
            throw SampleNotFoundException("등록되지 않은 샘플(${request.sampleId}) 입니다")
        }
    }

}
