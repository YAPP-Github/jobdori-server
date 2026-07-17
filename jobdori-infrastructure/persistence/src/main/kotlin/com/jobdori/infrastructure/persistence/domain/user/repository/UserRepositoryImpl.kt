package com.jobdori.infrastructure.persistence.domain.user.repository

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.repository.UserRepository
import com.jobdori.core.support.crypto.StringEncryptor
import com.jobdori.infrastructure.persistence.domain.user.entity.UserEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository,
    private val encryptor: StringEncryptor,
) : UserRepository {

    @Transactional(readOnly = true)
    override fun findById(id: Long): User? {
        return jpaRepository.findByIdOrNull(id)?.toUser(encryptor)
    }

    @Transactional(readOnly = true)
    override fun findByPublicId(publicId: String): User? {
        return jpaRepository.findByPublicId(publicId)?.toUser(encryptor)
    }

    @Transactional
    override fun save(user: User): User {
        val entity = jpaRepository.save(UserEntity.from(user, encryptor))
        return entity.toUser(encryptor)
    }

    @Transactional
    override fun deleteById(id: Long) {
        jpaRepository.deleteById(id)
    }

}
