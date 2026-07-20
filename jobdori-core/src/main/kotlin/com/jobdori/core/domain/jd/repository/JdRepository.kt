package com.jobdori.core.domain.jd.repository

import com.jobdori.core.domain.jd.Jd

interface JdRepository {
    fun save(jd: Jd): Jd
    fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): Jd?
    fun findAllByIdsAndWorkspaceId(ids: Collection<Long>, workspaceId: Long): List<Jd>
    fun findByPublicIdAndWorkspaceId(publicId: String, workspaceId: Long): Jd?
    fun findAllByWorkspaceId(workspaceId: Long): List<Jd>
}
