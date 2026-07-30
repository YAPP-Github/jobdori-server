package com.jobdori.api.application.credit.controller

import com.jobdori.api.application.credit.dto.response.CreditResponse
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.credit.CreditService
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class CreditQueryResolver(
    private val creditService: CreditService,
) {

    @QueryMapping
    fun credit(@UserId userId: Long): CreditResponse =
        CreditResponse.from(creditService.getBalance(userId))

}
