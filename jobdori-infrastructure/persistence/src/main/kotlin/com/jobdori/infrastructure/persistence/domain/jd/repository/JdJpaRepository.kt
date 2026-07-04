package com.jobdori.infrastructure.persistence.domain.jd.repository

import com.jobdori.infrastructure.persistence.domain.jd.entity.JdEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JdJpaRepository : JpaRepository<JdEntity, Long> {
    fun findByPublicIdAndUserId(publicId: String, userId: Long): JdEntity?
    fun findAllByUserId(userId: Long): List<JdEntity>
}
