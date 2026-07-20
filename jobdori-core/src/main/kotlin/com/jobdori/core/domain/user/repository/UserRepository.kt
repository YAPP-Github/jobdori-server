package com.jobdori.core.domain.user.repository

import com.jobdori.core.domain.user.User

interface UserRepository {

    fun findById(id: Long): User?

    fun findByPublicId(publicId: String): User?

    fun save(user: User): User

    fun deleteById(id: Long)

}
