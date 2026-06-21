package com.jobdori.api.support.auth.rest

import com.jobdori.api.support.auth.UserId
import com.jobdori.common.error.InternalServerException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class UserIdArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(UserId::class.java) && parameter.parameterType == Long::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Long {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw InternalServerException("HttpServletRequest를 가져올 수 없습니다")
        return request.getAttribute(AuthRequestContext.USER_ID) as Long
    }

}
