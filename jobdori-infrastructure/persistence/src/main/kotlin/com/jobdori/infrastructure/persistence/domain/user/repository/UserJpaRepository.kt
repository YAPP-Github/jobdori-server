package com.jobdori.infrastructure.persistence.domain.user.repository

import com.jobdori.infrastructure.persistence.domain.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, Long> {

    fun findByPublicId(publicId: String): UserEntity?

}
