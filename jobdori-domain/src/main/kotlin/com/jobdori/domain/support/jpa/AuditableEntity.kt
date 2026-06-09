package com.jobdori.domain.support.jpa

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditableEntity {

    @CreatedDate
    var createdAt: Instant = Instant.now()

    @LastModifiedDate
    var updatedAt: Instant = Instant.now()

}
