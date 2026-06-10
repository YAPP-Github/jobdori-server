package com.jobdori.infrastructure.persistence.sample

import com.jobdori.infrastructure.persistence.sample.entity.SampleEntity

object SampleFixture {

    fun entity(
        name: String = "sample",
    ) = SampleEntity(
        name = name,
    )

}
