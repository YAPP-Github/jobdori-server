package com.untitled.domain.domain.member

import com.untitled.domain.support.jpa.AuditableEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class MemberEntity(
    var name: String,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun toMember(): Member {
        return Member(
            id = this.id,
            name = this.name,
        )
    }

}
