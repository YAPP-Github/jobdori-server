package com.jobdori.infrastructure.persistence.domain.keyword.entity

import com.jobdori.core.domain.keyword.KeywordType
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import com.jobdori.infrastructure.persistence.support.sequence.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Table(
    name = "keyword_dictionary_v1",
    uniqueConstraints = [UniqueConstraint(name = "ux_keyword_dictionary_v1_type_name", columnNames = ["type", "name"])],
)
@Entity
class KeywordDictionaryEntity(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var type: KeywordType,

    @Column(nullable = false, length = 100)
    var name: String,
) : AuditableEntity() {

    @Id
    @SnowflakeId
    var id: Long = 0L

}
