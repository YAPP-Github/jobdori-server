package com.jobdori.infrastructure.persistence.sample

import com.jobdori.infrastructure.IntegrationTest
import com.jobdori.infrastructure.persistence.sample.repository.SampleJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@IntegrationTest
class SampleRepositoryTest(
    private val sampleJpaRepository: SampleJpaRepository,
) : StringSpec({

    afterEach {
        sampleJpaRepository.deleteAll()
    }

    "새로운 샘플을 등록합니다" {
        // given
        val sample = SampleFixture.entity(name = "무제")

        // when
        sampleJpaRepository.save(sample)

        // then
        val samples = sampleJpaRepository.findAll()
        samples shouldHaveSize 1
        samples[0].name shouldBe "무제"
    }

    "이름으로 샘플을 조회합니다" {
        // given
        sampleJpaRepository.save(SampleFixture.entity(name = "무제"))

        // when
        val result = sampleJpaRepository.findByName("무제")

        // then
        result shouldNotBe null
        result?.name shouldBe "무제"
    }

    "존재하지 않는 이름으로 조회하면 null을 반환합니다" {
        // when
        val result = sampleJpaRepository.findByName("없는샘플")

        // then
        result.shouldBeNull()
    }

})
