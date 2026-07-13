package com.jobdori.common.sequence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThan

class SnowflakeIdGeneratorTest : StringSpec({

    val generator = SnowflakeIdGenerator()

    "SnowflakeId를 채번한다" {
        // when
        val one = generator.nextId()
        val two = generator.nextId()
        val three = generator.nextId()

        // then
        one shouldBeLessThan two
        two shouldBeLessThan three
    }

})
