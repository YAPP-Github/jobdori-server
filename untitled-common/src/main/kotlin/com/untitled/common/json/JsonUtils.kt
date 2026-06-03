package com.untitled.common.json

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.cfg.EnumFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule

object JsonUtils {

    private val KOTLIN_MODULE: KotlinModule = KotlinModule.Builder()
        .withReflectionCacheSize(2048)
        .disable(KotlinFeature.NullToEmptyCollection)
        .disable(KotlinFeature.NullToEmptyMap)
        .disable(KotlinFeature.NullIsSameAsDefault)
        .disable(KotlinFeature.SingletonSupport)
        .disable(KotlinFeature.NewStrictNullChecks)
        .build()

    val DEFAULT_JSON_MAPPER_BUILDER: JsonMapper.Builder = JsonMapper.builder()
        .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .changeDefaultPropertyInclusion { inclusion -> inclusion.withValueInclusion(NON_NULL) }
        .addModules(KOTLIN_MODULE)

    val DEFAULT_JSON_MAPPER: JsonMapper = DEFAULT_JSON_MAPPER_BUILDER.build()

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
