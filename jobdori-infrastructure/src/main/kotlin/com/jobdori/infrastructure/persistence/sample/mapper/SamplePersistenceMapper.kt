package com.jobdori.infrastructure.persistence.sample.mapper

import com.jobdori.core.domain.sample.model.Sample
import com.jobdori.core.domain.sample.vo.SampleName
import com.jobdori.infrastructure.persistence.sample.entity.SampleEntity
import org.springframework.stereotype.Component

@Component
class SamplePersistenceMapper {
    fun toDomain(entity: SampleEntity): Sample = Sample(id = entity.id, name = SampleName(entity.name))
    fun toEntity(sample: Sample): SampleEntity = SampleEntity(name = sample.name.value).apply { id = sample.id }
}
