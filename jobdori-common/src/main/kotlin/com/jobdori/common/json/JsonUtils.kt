package com.jobdori.common.json

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import tools.jackson.databind.cfg.EnumFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

object JsonUtils {

    val DEFAULT_JSON_MAPPER: JsonMapper = JsonMapper.builder()
        .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
        .changeDefaultPropertyInclusion { inclusion -> inclusion.withValueInclusion(NON_NULL) }
        .addModule(KotlinModule.Builder().build())
        .build()

    fun <T> toObject(input: String, toClass: Class<T>): T? {
        return try {
            DEFAULT_JSON_MAPPER.readValue(input, toClass)
        } catch (exception: Exception) {
            throw IllegalArgumentException(
                "역직렬화 중 에러가 발생하였습니다. input: ($input) toClass: (${toClass.simpleName})",
                exception,
            )
        }
    }

    fun <T> toJson(input: T): String {
        return try {
            DEFAULT_JSON_MAPPER.writeValueAsString(input)
        } catch (exception: Exception) {
            throw IllegalArgumentException("직렬화 중 에러가 발생하였습니다. input: ($input)", exception)
        }
    }

}
