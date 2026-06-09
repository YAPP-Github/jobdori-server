package com.jobdori.domain.domain.sample

import com.jobdori.domain.support.jpa.AuditableEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class SampleEntity(
    var name: String,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun update(name: String) {
        this.name = name
    }

    fun toSample(): Sample {
        return Sample(
            id = this.id,
            name = this.name,
        )
    }

}
