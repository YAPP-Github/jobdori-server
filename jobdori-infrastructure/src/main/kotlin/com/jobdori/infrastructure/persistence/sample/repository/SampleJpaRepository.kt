package com.jobdori.infrastructure.persistence.sample.repository

import com.jobdori.infrastructure.persistence.sample.entity.SampleEntity
import org.springframework.data.jpa.repository.JpaRepository

// SampleJpaRepository.kt  (★ 기존 SampleRepository.kt 리네임)
interface SampleJpaRepository : JpaRepository<SampleEntity, Long> {
    fun findByName(name: String): SampleEntity?
}
// SampleCustomRepository.kt / SampleCustomRepository.kt  : 내용 동일, import 경로만 변경
