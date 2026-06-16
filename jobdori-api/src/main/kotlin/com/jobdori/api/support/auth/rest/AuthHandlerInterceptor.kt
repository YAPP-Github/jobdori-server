package com.jobdori.api.support.auth.rest

import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.BearerTokenExtractor
import com.jobdori.core.application.auth.AuthUserReadService
import com.jobdori.core.application.auth.error.InvalidAuthTokenException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthHandlerInterceptor(
    private val authUserReadServiceProvider: ObjectProvider<AuthUserReadService>,
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler !is HandlerMethod || !handler.hasMethodAnnotation(Authenticated::class.java)) {
            return true
        }

        val accessToken = BearerTokenExtractor.extract(
            request.getHeader(HttpHeaders.AUTHORIZATION),
        )
        val authUserReader = authUserReadServiceProvider.getIfAvailable()
            ?: throw InvalidAuthTokenException("인증 사용자 조회 서비스를 사용할 수 없습니다")

        val userId = authUserReader.getUserId(accessToken)

        request.setAttribute(AuthRequestContext.USER_ID, userId)

        return true
    }

}
