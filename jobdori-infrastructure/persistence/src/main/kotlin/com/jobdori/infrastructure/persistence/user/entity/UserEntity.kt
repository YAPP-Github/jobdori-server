package com.jobdori.infrastructure.persistence.user.entity

import com.jobdori.core.domain.user.User
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity(
    @Column(nullable = false, unique = true, updatable = false)
    var publicId: String,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toUser() = User(
        id = id,
        publicId = publicId,
    )

    companion object {
        fun from(user: User) = UserEntity(
            publicId = user.publicId,
        ).also { it.id = user.id }
    }

}
