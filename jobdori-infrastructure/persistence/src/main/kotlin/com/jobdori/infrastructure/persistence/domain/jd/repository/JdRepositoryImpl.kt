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
    override fun findByPublicIdAndWorkspaceId(publicId: String, workspaceId: Long): Jd? =
        jdJpa.findByPublicIdAndWorkspaceId(publicId, workspaceId)?.toDomain()

    @Transactional(readOnly = true)
    override fun findAllByWorkspaceId(workspaceId: Long): List<Jd> =
        jdJpa.findAllByWorkspaceId(workspaceId).map { it.toDomain() }

}
