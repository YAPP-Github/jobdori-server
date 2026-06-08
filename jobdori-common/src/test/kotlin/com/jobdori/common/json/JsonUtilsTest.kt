package com.jobdori.common.json

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class JsonsTest : StringSpec({

    "toObject는 Class 타입으로 JSON 문자열을 객체로 역직렬화한다" {
        // given
        val input = """{"name":"무제","age":20,"unknown":"ignored"}"""

        // when
        val result = JsonUtils.toObject(input = input, toClass = Sample::class.java)

        // then
        result shouldBe Sample(name = "무제", age = 20)
    }

    "toJson은 객체를 JSON 문자열로 직렬화하고 null 필드는 제외한다" {
        // given
        val input = Sample(name = "무제", age = 20, memo = null)

        // when
        val result = JsonUtils.toJson(input)

        // then
        result shouldBe """{"name":"무제","age":20}"""
    }

    "toObject 역직렬화 실패 시 IllegalArgumentException을 던진다" {
        // given
        val input = """{"name":"""

        // when
        val exception = shouldThrow<IllegalArgumentException> {
            JsonUtils.toObject(input = input, toClass = Sample::class.java)
        }

        // then
        exception.message shouldBe "역직렬화 중 에러가 발생하였습니다. input: ($input) toClass: (Sample)"
    }

})

private data class Sample(
    val name: String,
    val age: Int,
    val memo: String? = null,
)
