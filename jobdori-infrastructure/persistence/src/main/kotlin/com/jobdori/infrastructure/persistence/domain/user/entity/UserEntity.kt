package com.jobdori.infrastructure.persistence.domain.user.entity

import com.jobdori.core.domain.user.User
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table(name = "user_v1")
@Entity
class UserEntity(
    @Column(nullable = false, length = 50, unique = true, updatable = false)
    var publicId: String,

    @Column(nullable = false, length = 320)
    var email: String,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(length = 300)
    var profileImageUrl: String?,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toUser() = User(
        id = id,
        publicId = publicId,
        email = email,
        name = name,
        profileImageUrl = profileImageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun from(user: User) = UserEntity(
            publicId = user.publicId,
            email = user.email,
            name = user.name,
            profileImageUrl = user.profileImageUrl,
        ).also { it.id = user.id }
    }

}
