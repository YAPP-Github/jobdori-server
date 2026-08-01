package com.jobdori.api.support.graphql

import com.jobdori.api.support.notification.AsyncErrorNotifier
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.core.domain.user.error.UserErrorCode
import com.jobdori.core.domain.user.error.UserNotFoundException
import graphql.execution.ExecutionStepInfo
import graphql.execution.ResultPath
import graphql.language.Field
import graphql.language.SourceLocation
import graphql.schema.DataFetchingEnvironment
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import org.springframework.beans.factory.ObjectProvider
import org.springframework.graphql.execution.ErrorType
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.validation.FieldError

internal class GraphQLExceptionHandlerTest : StringSpec({

    val errorNotifier = mockk<AsyncErrorNotifier>(relaxed = true)
    val errorNotifierProvider = mockk<ObjectProvider<AsyncErrorNotifier>> {
        every { ifAvailable } returns errorNotifier
    }
    val handler = GraphQLExceptionAdvice(errorNotifierProvider)

    "ConstraintViolationException은 invalid_arguments와 details를 GraphQL error로 변환한다" {
        // given
        val exception = ConstraintViolationException(
            setOf(
                constraintViolation(
                    field = "sampleId",
                    message = "sampleId is blank",
                )
            )
        )
        val env = dataFetchingEnvironment()

        // when
        val error = handler.handleConstraintViolationException(exception, env)

        // then
        error.errorType shouldBe ErrorType.BAD_REQUEST
        error.message shouldBe CommonErrorCode.E400_INVALID_ARGUMENTS.message
        error.locations shouldContainExactly listOf(SOURCE_LOCATION)
        error.path shouldBe RESULT_PATH
        error.extensions shouldContainExactly mapOf(
            "code" to CommonErrorCode.E400_INVALID_ARGUMENTS.code,
            "details" to listOf(
                mapOf(
                    "field" to "sampleId",
                    "reason" to "sampleId is blank",
                ),
            ),
        )
    }

    "BindException은 invalid_arguments와 fieldErrors를 GraphQL error로 변환한다" {
        // given
        val bindingResult = BeanPropertyBindingResult(mapOf("sampleId" to ""), "request")
        bindingResult.addError(
            FieldError(
                "request",
                "sampleId",
                "",
                false,
                null,
                null,
                "sampleId is blank",
            )
        )
        val exception = BindException(bindingResult)
        val env = dataFetchingEnvironment()

        // when
        val error = handler.handleBindException(exception, env)

        // then
        error.errorType shouldBe ErrorType.BAD_REQUEST
        error.message shouldBe CommonErrorCode.E400_INVALID_ARGUMENTS.message
        error.locations shouldContainExactly listOf(SOURCE_LOCATION)
        error.path shouldBe RESULT_PATH
        error.extensions shouldContainExactly mapOf(
            "code" to CommonErrorCode.E400_INVALID_ARGUMENTS.code,
            "details" to listOf(
                mapOf(
                    "field" to "sampleId",
                    "reason" to "sampleId is blank",
                ),
            ),
        )
    }

    "BaseException에 details가 없으면 code만 extensions에 포함한다" {
        // given
        val exception = UserNotFoundException(message = "User not found. sampleId=1")
        val env = dataFetchingEnvironment()

        // when
        val error = handler.handleBaseException(exception, env)

        // then
        error.errorType shouldBe ErrorType.NOT_FOUND
        error.message shouldBe UserErrorCode.E404_USER_NOT_FOUND.message
        error.locations shouldContainExactly listOf(SOURCE_LOCATION)
        error.path shouldBe RESULT_PATH
        error.extensions shouldContainExactly mapOf(
            "code" to UserErrorCode.E404_USER_NOT_FOUND.code,
        )
        verify(exactly = 0) { errorNotifier.notify(any(), any()) }
    }

    "BaseException이 아니면 internal error로 변환한다" {
        // given
        val exception = IllegalStateException("Unexpected error")
        val env = dataFetchingEnvironment()

        // when
        val error = handler.handleThrowable(exception, env)

        // then
        error.errorType shouldBe ErrorType.INTERNAL_ERROR
        error.message shouldBe CommonErrorCode.E500_INTERNAL_ERROR.message
        error.locations shouldContainExactly listOf(SOURCE_LOCATION)
        error.path shouldBe RESULT_PATH
        error.extensions shouldContainExactly mapOf(
            "code" to CommonErrorCode.E500_INTERNAL_ERROR.code,
        )
        verify(exactly = 1) {
            errorNotifier.notify(CommonErrorCode.E500_INTERNAL_ERROR.code, exception)
        }
    }

})

private val SOURCE_LOCATION = SourceLocation(1, 2)
private val RESULT_PATH = listOf("sample", "name")

private fun constraintViolation(
    field: String,
    message: String,
): ConstraintViolation<Any> {
    val node = mockk<Path.Node> {
        every { name } returns field
    }
    val path = mockk<Path> {
        every { iterator() } returns mutableListOf(node).iterator()
    }
    return mockk {
        every { propertyPath } returns path
        every { this@mockk.message } returns message
    }
}

private fun dataFetchingEnvironment(): DataFetchingEnvironment {
    val field = Field.newField("sample")
        .sourceLocation(SOURCE_LOCATION)
        .build()
    val executionStepInfo = mockk<ExecutionStepInfo>()

    every { executionStepInfo.path } returns ResultPath.fromList(RESULT_PATH)

    return mockk {
        every { this@mockk.field } returns field
        every { this@mockk.executionStepInfo } returns executionStepInfo
    }
}
