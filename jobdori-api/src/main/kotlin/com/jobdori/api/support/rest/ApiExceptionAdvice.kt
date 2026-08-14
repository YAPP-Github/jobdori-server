package com.jobdori.api.support.rest

import com.jobdori.common.error.BaseException
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.FileErrorCode
import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.api.support.notification.AsyncErrorNotifier
import org.springframework.beans.TypeMismatchException
import org.springframework.beans.factory.ObjectProvider
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
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import tools.jackson.databind.exc.MismatchedInputException

@RestControllerAdvice
class ApiExceptionAdvice(
    private val errorNotifierProvider: ObjectProvider<AsyncErrorNotifier>,
) {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException::class)
    fun handleBadRequest(exception: BindException): ApiResponse<Nothing> {
        log.warn(exception) { exception.message }
        val details = exception.bindingResult.fieldErrors.map { fieldError: FieldError ->
            ErrorDetail(
                field = fieldError.field,
                reason = fieldError.defaultMessage.orEmpty(),
            )
        }
        return ApiResponse.fail(
            error = CommonErrorCode.E400_INVALID_ARGUMENTS,
            message = details.firstOrNull()?.reason ?: CommonErrorCode.E400_INVALID_ARGUMENTS.message,
            details = details,
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(exception: MaxUploadSizeExceededException): ApiResponse<Nothing> {
        log.warn(exception) { exception.message }
        return ApiResponse.fail(FileErrorCode.E400_FILE_SIZE_EXCEEDED)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException::class, NoHandlerFoundException::class)
    fun handleNoResourceFoundException(exception: Exception): ResponseEntity<Nothing> {
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
        if (exception.errorCode.httpStatusCode >= 500) {
            log.atError {
                message = exception.message
                cause = exception
                payload = mapOf("errorCode" to exception.errorCode.code)
            }
            errorNotifierProvider.ifAvailable?.notify(exception.errorCode.code, exception)
        } else {
            log.warn { "${exception.errorCode.code}: ${exception.message}" }
        }
        return ResponseEntity.status(exception.errorCode.httpStatusCode)
            .body(ApiResponse.fail(error = exception.errorCode, details = exception.details))
    }

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<ApiResponse<Nothing>> {
        log.atError {
            message = throwable.message
            cause = throwable
            payload = mapOf("errorCode" to CommonErrorCode.E500_INTERNAL_ERROR.code)
        }
        errorNotifierProvider.ifAvailable?.notify(CommonErrorCode.E500_INTERNAL_ERROR.code, throwable)
        return ResponseEntity.internalServerError()
            .body(ApiResponse.fail(CommonErrorCode.E500_INTERNAL_ERROR))
    }

}
