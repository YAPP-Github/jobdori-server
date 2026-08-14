package com.jobdori.api.support.rest

import com.jobdori.api.support.notification.AsyncErrorNotifier
import com.jobdori.common.error.CommonErrorCode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.springframework.beans.factory.ObjectProvider
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindException
import org.springframework.validation.FieldError

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
})
