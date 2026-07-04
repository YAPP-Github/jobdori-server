package com.jobdori.infrastructure.persistence.domain.jd.repository

import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.repository.JdRepository
import com.jobdori.infrastructure.persistence.domain.jd.entity.JdEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JdRepositoryImpl(
    private val jdJpa: JdJpaRepository,
) : JdRepository {

    @Transactional
    override fun save(jd: Jd): Jd = jdJpa.save(JdEntity.from(jd)).toDomain()

    @Transactional(readOnly = true)
    override fun findByPublicIdAndUserId(publicId: String, userId: Long): Jd? =
        jdJpa.findByPublicIdAndUserId(publicId, userId)?.toDomain()

    @Transactional(readOnly = true)
    override fun findAllByUserId(userId: Long): List<Jd> =
        jdJpa.findAllByUserId(userId).map { it.toDomain() }

}
