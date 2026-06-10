package com.jobdori.infrastructure.persistence.sample.repository

import com.jobdori.infrastructure.persistence.sample.entity.SampleEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SampleJpaRepository : JpaRepository<SampleEntity, Long> {
    fun findByName(name: String): SampleEntity?
}
