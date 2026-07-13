package com.jobdori.infrastructure.persistence.support.sequence

import com.jobdori.common.sequence.IdGenerator
import com.jobdori.common.sequence.SnowflakeIdGenerator
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator

class SnowflakeHibernateIdGenerator(
    private val idGenerator: IdGenerator = SnowflakeIdGenerator(),
) : IdentifierGenerator {

    override fun generate(session: SharedSessionContractImplementor, `object`: Any): Any {
        return idGenerator.nextId()
    }

}
