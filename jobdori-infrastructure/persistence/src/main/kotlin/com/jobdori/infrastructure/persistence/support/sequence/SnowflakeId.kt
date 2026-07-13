package com.jobdori.infrastructure.persistence.support.sequence

import org.hibernate.annotations.IdGeneratorType

@Target(AnnotationTarget.FIELD)
@IdGeneratorType(value = SnowflakeHibernateIdGenerator::class)
annotation class SnowflakeId
