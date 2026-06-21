package com.jobdori.infrastructure.persistence.user.repository

import com.jobdori.infrastructure.persistence.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, Long> {

    fun findByPublicId(publicId: String): UserEntity?

}
