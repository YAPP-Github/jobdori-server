package com.jobdori.api.support.graphql

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.IntValue
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import java.math.BigInteger
import java.util.Locale

@Configuration
class GraphQLLongScalarConfig {

    @Bean
    fun longScalarRuntimeWiringConfigurer(): RuntimeWiringConfigurer {
        return RuntimeWiringConfigurer { wiringBuilder ->
            wiringBuilder.scalar(LONG_SCALAR)
        }
    }

    companion object {
        private val LONG_SCALAR: GraphQLScalarType = GraphQLScalarType.newScalar()
            .name("Long")
            .description("64-bit signed integer")
            .coercing(LongCoercing)
            .build()
    }

    private object LongCoercing : Coercing<Long, Long> {

        override fun serialize(dataFetcherResult: Any, graphQLContext: GraphQLContext, locale: Locale): Long {
            return coerceLong(dataFetcherResult)
                ?: throw CoercingSerializeException("Expected a Long-compatible value")
        }

        override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): Long {
            return coerceLong(input)
                ?: throw CoercingParseValueException("Expected a Long-compatible value")
        }

        override fun parseLiteral(
            input: Value<*>,
            variables: CoercedVariables,
            graphQLContext: GraphQLContext,
            locale: Locale,
        ): Long {
            return when (input) {
                is IntValue -> input.value.toLongExactOrNull()
                is StringValue -> input.value?.toLongOrNull()
                else -> null
            } ?: throw CoercingParseLiteralException("Expected a Long-compatible literal")
        }

        override fun valueToLiteral(input: Any, graphQLContext: GraphQLContext, locale: Locale): Value<*> {
            val value = coerceLong(input)
                ?: throw CoercingSerializeException("Expected a Long-compatible value")
            return IntValue(BigInteger.valueOf(value))
        }

        private fun coerceLong(value: Any): Long? {
            return when (value) {
                is Long -> value
                is Int -> value.toLong()
                is Short -> value.toLong()
                is Byte -> value.toLong()
                is BigInteger -> value.toLongExactOrNull()
                is String -> value.toLongOrNull()
                else -> null
            }
        }

        private fun BigInteger.toLongExactOrNull(): Long? {
            return runCatching { longValueExact() }.getOrNull()
        }
    }

}
