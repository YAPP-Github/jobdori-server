package com.jobdori.api.support.graphql

import com.jobdori.common.error.CommonErrorCode
import graphql.GraphQLError
import graphql.language.SourceLocation
import graphql.validation.ValidationError
import graphql.validation.ValidationErrorType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.springframework.graphql.execution.ErrorType

internal class GraphQLValidationErrorInterceptorTest : StringSpec({

    val interceptor = GraphQLValidationErrorInterceptor()

    "GraphQL validation error를 invalid_arguments로 변환한다" {
        // given
        val location = SourceLocation(5, 5)
        val validationError = ValidationError.newValidationError()
            .validationErrorType(ValidationErrorType.WrongType)
            .description(
                "Validation error (WrongType@[updateExperienceProject]) : " +
                    "argument 'request.period' with value 'ObjectValue{...}' must be an object type"
            )
            .sourceLocation(location)
            .build()

        // when
        val error = interceptor.transformError(validationError)

        // then
        error.errorType shouldBe ErrorType.BAD_REQUEST
        error.message shouldBe CommonErrorCode.E400_INVALID_ARGUMENTS.message
        error.locations shouldContainExactly listOf(location)
        error.extensions shouldContainExactly mapOf(
            "code" to CommonErrorCode.E400_INVALID_ARGUMENTS.code,
            "details" to listOf(
                mapOf(
                    "field" to "request.period",
                    "reason" to "입력 형식이 올바르지 않습니다.",
                ),
            ),
        )
    }

    "필드 경로를 추출할 수 없는 validation error는 details 없이 변환한다" {
        // given
        val validationError = ValidationError.newValidationError()
            .validationErrorType(ValidationErrorType.UnknownOperation)
            .description("Unknown operation named 'sample'")
            .build()

        // when
        val error = interceptor.transformError(validationError)

        // then
        error.extensions shouldContainExactly mapOf(
            "code" to CommonErrorCode.E400_INVALID_ARGUMENTS.code,
        )
    }

    "GraphQL validation error가 아니면 변경하지 않는다" {
        // given
        val original = GraphQLError.newError()
            .message("Resolver error")
            .errorType(ErrorType.INTERNAL_ERROR)
            .build()

        // when
        val error = interceptor.transformError(original)

        // then
        error shouldBeSameInstanceAs original
    }

})
