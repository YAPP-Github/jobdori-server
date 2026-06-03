package com.untitled.api

import com.untitled.domain.support.spring.JsonConfig
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles
import kotlin.reflect.KClass

@ActiveProfiles("test")
@Import(JsonConfig::class)
@WebMvcTest
annotation class ApiTest(
    @get:AliasFor(
        annotation = WebMvcTest::class,
        attribute = "value",
    ) vararg val value: KClass<*> = [],
)
