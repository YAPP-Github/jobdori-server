package com.jobdori.api.support.graphql

import com.jobdori.common.error.BaseException
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.error.ErrorCode
import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.logger.LoggerExtension.log
import graphql.GraphQLError
import graphql.schema.DataFetchingEnvironment
import jakarta.validation.ConstraintViolationException
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.graphql.execution.ErrorType
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
class GraphQLExceptionAdvice {

    @GraphQlExceptionHandler
    fun handleConstraintViolationException(
        exception: ConstraintViolationException,
        env: DataFetchingEnvironment,
    ): GraphQLError {
        return generateGraphQLError(
            errorCode = CommonErrorCode.E400_INVALID_ARGUMENTS,
            details = exception.constraintViolations.map {
                ErrorDetail(
                    field = it.propertyPath.lastOrNull()?.name.orEmpty(),
                    reason = it.message,
                )
            },
            env = env,
        )
    }

    @GraphQlExceptionHandler
    fun handleBindException(
        exception: BindException,
        env: DataFetchingEnvironment,
    ): GraphQLError {
        return generateGraphQLError(
            errorCode = CommonErrorCode.E400_INVALID_ARGUMENTS,
            details = exception.fieldErrors.map {
                ErrorDetail(
                    field = it.field,
                    reason = it.defaultMessage.orEmpty(),
                )
            },
            env = env,
        )
    }

    @GraphQlExceptionHandler
    fun handleBaseException(
        exception: BaseException,
        env: DataFetchingEnvironment,
    ): GraphQLError {
        log.error(exception) { exception.message }
        return generateGraphQLError(
            errorCode = exception.errorCode,
            env = env,
        )
    }

    @GraphQlExceptionHandler
    fun handleThrowable(
        exception: Throwable,
        env: DataFetchingEnvironment,
    ): GraphQLError {
        log.error(exception) { exception.message }
        return generateGraphQLError(
            errorCode = CommonErrorCode.E500_INTERNAL_ERROR,
            env = env,
        )
    }

    private fun generateGraphQLError(
        errorCode: ErrorCode,
        details: List<ErrorDetail> = emptyList(),
        env: DataFetchingEnvironment,
    ): GraphQLError {
        return GraphQLError.newError().errorType(toGraphQlErrorType(errorCode)).message("{{TBD}}") // TODO
            .extensions(generateExtensions(errorCode, details)).location(env.field.sourceLocation)
            .path(env.executionStepInfo.path).build()
    }

    private fun generateExtensions(
        errorCode: ErrorCode,
        details: List<ErrorDetail> = emptyList(),
    ): Map<String, Any> = buildMap {
        put("code", errorCode.code)
        if (details.isNotEmpty()) {
            put("details", details.map {
                mapOf(
                    "field" to it.field,
                    "reason" to it.reason,
                )
            })
        }
    }

    private fun toGraphQlErrorType(errorCode: ErrorCode): ErrorType {
        return when (errorCode.httpStatusCode) {
            400 -> ErrorType.BAD_REQUEST
            401 -> ErrorType.UNAUTHORIZED
            403 -> ErrorType.FORBIDDEN
            404 -> ErrorType.NOT_FOUND
            else -> ErrorType.INTERNAL_ERROR
        }
    }

}
