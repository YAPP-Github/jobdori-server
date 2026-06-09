package com.jobdori.api.application.sample.resolver

import com.jobdori.api.application.sample.SampleStore
import com.jobdori.api.application.sample.dto.request.SampleGetRequest
import com.jobdori.api.application.sample.dto.response.SampleResponse
import com.jobdori.core.domain.sample.error.SampleNotFoundException
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class SampleQueryResolver {

    @QueryMapping
    fun sample(
        @Argument @Valid request: SampleGetRequest,
    ): SampleResponse {
        return SampleStore.findById(request.sampleId)
            ?: throw SampleNotFoundException("등록되지 않은 샘플(${request.sampleId}) 입니다")
    }

    @QueryMapping
    fun samples(): List<SampleResponse> {
        return SampleStore.findAll()
    }

}
