package com.jobdori.api.application.sample

import com.jobdori.domain.domain.sample.SampleNotFoundException
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class SampleMutationResolver {

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
        return SampleStore.update(
            sampleId = request.sampleId,
            name = request.name,
        ) ?: throw SampleNotFoundException("등록되지 않은 샘플(${request.sampleId}) 입니다")
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
