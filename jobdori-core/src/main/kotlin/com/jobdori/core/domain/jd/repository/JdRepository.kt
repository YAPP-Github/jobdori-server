package com.jobdori.core.domain.jd.repository

import com.jobdori.core.domain.jd.Jd

interface JdRepository {
    fun save(jd: Jd): Jd
    fun findByPublicIdAndUserId(publicId: String, userId: Long): Jd?
    fun findAllByUserId(userId: Long): List<Jd>
}
