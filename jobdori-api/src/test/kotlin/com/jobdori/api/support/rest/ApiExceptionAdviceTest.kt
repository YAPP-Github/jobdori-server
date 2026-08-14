package com.jobdori.api.support.rest

import com.jobdori.api.support.notification.AsyncErrorNotifier
import com.jobdori.common.error.CommonErrorCode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpStatus
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.multipart.MultipartException

internal class ApiExceptionAdviceTest : StringSpec({

    val errorNotifierProvider = mockk<ObjectProvider<AsyncErrorNotifier>>(relaxed = true)
    val advice = ApiExceptionAdvice(errorNotifierProvider)

    "validation 메시지를 REST error message와 details에 반환한다" {
        val bindingResult = BeanPropertyBindingResult(mapOf("name" to ""), "request")
        bindingResult.addError(
            FieldError(
                "request",
                "name",
                "",
                false,
                null,
                null,
                "이름을 입력해 주세요.",
            )
        )

        val response = advice.handleBadRequest(BindException(bindingResult))
        val error = requireNotNull(response.error)

        error.code shouldBe CommonErrorCode.E400_INVALID_ARGUMENTS.code
        error.message shouldBe "이름을 입력해 주세요."
        error.details shouldHaveSize 1
        error.details.single().field shouldBe "name"
        error.details.single().reason shouldBe "이름을 입력해 주세요."
    }

    "multipart 파싱 실패는 internal error를 반환하되 알림은 보내지 않는다" {
        val response = advice.handleMultipartException(MultipartException("Failed to parse multipart servlet request"))

        response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
        response.body?.error?.code shouldBe CommonErrorCode.E500_INTERNAL_ERROR.code
        verify(exactly = 0) { errorNotifierProvider.ifAvailable }
    }

    "다른 multipart 오류는 알림을 보낸다" {
        val notifier = mockk<AsyncErrorNotifier>(relaxed = true)
        val notifierProvider = mockk<ObjectProvider<AsyncErrorNotifier>>()
        val multipartAdvice = ApiExceptionAdvice(notifierProvider)
        io.mockk.every { notifierProvider.ifAvailable } returns notifier
        val exception = MultipartException("Other multipart error")

        multipartAdvice.handleMultipartException(exception)

        verify(exactly = 1) {
            notifier.notify(CommonErrorCode.E500_INTERNAL_ERROR.code, exception)
        }
    }
})
