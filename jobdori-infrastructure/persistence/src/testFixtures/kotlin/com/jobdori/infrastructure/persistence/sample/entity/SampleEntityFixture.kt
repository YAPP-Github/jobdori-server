package com.jobdori.infrastructure.persistence.sample.entity

object SampleEntityFixture {

    fun entity(
        name: String = "sample",
    ) = SampleEntity(
        name = name,
    )

}