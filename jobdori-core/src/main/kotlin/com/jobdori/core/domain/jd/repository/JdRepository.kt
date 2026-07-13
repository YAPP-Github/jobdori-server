package com.jobdori.core.domain.jd.repository

import com.jobdori.core.domain.jd.Jd

interface JdRepository {
    fun save(jd: Jd): Jd
    fun findByPublicIdAndWorkspaceId(publicId: String, workspaceId: Long): Jd?
    fun findAllByWorkspaceId(workspaceId: Long): List<Jd>
    fun deleteById(id: Long)
}
