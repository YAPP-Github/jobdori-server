package com.jobdori.infrastructure.persistence.user.repository

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.repository.UserRepository
import com.jobdori.infrastructure.persistence.user.entity.UserEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository,
) : UserRepository {

    override fun findById(id: Long): User? {
        return jpaRepository.findByIdOrNull(id)?.toUser()
    }

    override fun findByPublicId(publicId: String): User? {
        return jpaRepository.findByPublicId(publicId)?.toUser()
    }

    override fun save(user: User): User {
        val entity = jpaRepository.save(UserEntity.from(user))
        return entity.toUser()
    }

}
