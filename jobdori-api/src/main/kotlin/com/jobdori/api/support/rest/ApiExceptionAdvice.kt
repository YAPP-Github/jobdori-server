package com.jobdori.api.support.rest

import com.jobdori.common.error.BaseException
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.logger.LoggerExtension.log
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import tools.jackson.databind.exc.MismatchedInputException

@RestControllerAdvice
class ApiExceptionAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException::class)
    fun handleBadRequest(exception: BindException): ApiResponse<Nothing> {
        log.warn(exception) { exception.message }
        return ApiResponse.fail(
            error = CommonErrorCode.E400_INVALID_ARGUMENTS,
            details = exception.bindingResult.fieldErrors.map { fieldError: FieldError ->
                ErrorDetail(
                    field = fieldError.field,
                    reason = fieldError.defaultMessage.orEmpty(),
                )
            }
        )
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(exception: HttpMessageNotReadableException): ApiResponse<Nothing> {
        log.warn(exception) { exception.message }
        if (exception.rootCause is MismatchedInputException) {
            return ApiResponse.fail(
                error = CommonErrorCode.E400_INVALID_ARGUMENTS,
                details = (exception.rootCause as MismatchedInputException).path.map { path ->
                    ErrorDetail(
                        field = path.propertyName,
                        reason = "Parameter is required",
                    )
                }
            )
        }
        return ApiResponse.fail(CommonErrorCode.E400_INVALID_ARGUMENTS)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TypeMismatchException::class)
    fun handleTypeMismatchException(exception: TypeMismatchException): ApiResponse<Nothing> {
        log.warn { exception.message }
        return ApiResponse.fail(CommonErrorCode.E400_INVALID_ARGUMENTS)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(exception: MissingServletRequestParameterException): ApiResponse<Nothing> {
        log.warn { exception.message }
        return ApiResponse.fail(
            error = CommonErrorCode.E400_INVALID_ARGUMENTS,
            details = listOf(
                ErrorDetail(
                    field = exception.parameterName,
                    reason = "Parameter is required",
                ),
            )
        )
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(exception: NoResourceFoundException): ResponseEntity<Nothing> {
        log.warn { exception.message }
        return ResponseEntity.notFound().build()
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(exception: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn { exception.message }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build()
    }

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(exception: BaseException): ResponseEntity<ApiResponse<Nothing>> {
        log.error(exception) { exception.message }
        return ResponseEntity.status(exception.errorCode.httpStatusCode)
            .body(ApiResponse.fail(error = exception.errorCode))
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<ApiResponse<Nothing>> {
        log.error(throwable) { throwable.message }
        return ResponseEntity.internalServerError()
            .body(ApiResponse.fail(CommonErrorCode.E500_INTERNAL_ERROR))
    }

}
