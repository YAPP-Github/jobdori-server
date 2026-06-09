package com.jobdori.api

import com.jobdori.core.support.spring.JsonConfig
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.ActiveProfiles
import kotlin.reflect.KClass

@ActiveProfiles("test")
@Import(
    JsonConfig::class,
)
@GraphQlTest
annotation class GraphQLTest(
    @get:AliasFor(
        annotation = GraphQlTest::class,
        attribute = "value",
    ) vararg val value: KClass<*> = [],
)
