package com.untitled.api.application.sample

import com.untitled.api.GraphQLTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity

@GraphQLTest(
    SampleQueryResolver::class,
    SampleMutationResolver::class,
)
internal class SampleResolverTest(
    private val graphQlTester: GraphQlTester,
) : StringSpec({

    beforeEach {
        SampleStore.reset(
            SampleResponse(sampleId = 1L, name = "1번 샘플"),
            SampleResponse(sampleId = 2L, name = "2번 샘플"),
        )
    }

    "sampleId로 샘플을 조회한다" {
        graphQlTester.documentName("sample")
            .variable("request", mapOf("sampleId" to 1L))
            .execute()
            .path("sample.sampleId").entity<Long>().isEqualTo(1L)
            .path("sample.name").entity<String>().isEqualTo("1번 샘플")
    }

    "등록된 샘플 목록을 조회한다" {
        graphQlTester.documentName("samples")
            .execute()
            .path("samples[0].name").entity<String>().isEqualTo("1번 샘플")
            .path("samples[1].name").entity<String>().isEqualTo("2번 샘플")
    }

    "샘플을 생성한다" {
        graphQlTester.documentName("create-sample")
            .variable("request", mapOf("name" to "생성된 샘플"))
            .execute()
            .path("createSample.sampleId").entity<Long>().isEqualTo(3L)
            .path("createSample.name").entity<String>().isEqualTo("생성된 샘플")

        SampleStore.findAll() shouldHaveSize 3
    }

    "샘플을 수정한다" {
        graphQlTester.documentName("update-sample")
            .variable("request", mapOf("sampleId" to 1L, "name" to "수정 후 샘플"))
            .execute()
            .path("updateSample.sampleId").entity<Long>().isEqualTo(1L)
            .path("updateSample.name").entity<String>().isEqualTo("수정 후 샘플")
    }

    "샘플을 삭제한다" {
        graphQlTester.documentName("delete-sample")
            .variable("request", mapOf("sampleId" to 1L))
            .execute()

        SampleStore.findAll() shouldHaveSize 1
    }

    "등록되지 않은 sampleId이면 GraphQL error를 반환한다" {
        graphQlTester.documentName("sample")
            .variable("request", mapOf("sampleId" to 3L))
            .execute()
            .errors()
            .satisfy { errors ->
                errors shouldHaveSize 1
                val error = errors.first()
                error.path shouldBe "sample"
                error.extensions shouldContain ("code" to "sample_not_found")
            }
    }

})
