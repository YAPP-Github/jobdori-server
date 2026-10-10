package com.jobdori.api.support.auth.graphql

import com.jobdori.api.support.auth.BearerTokenExtractor
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.logging.MdcKeys
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.domain.auth.error.InvalidAuthTokenException
import graphql.schema.DataFetchingEnvironment
import org.slf4j.MDC
import org.springframework.core.MethodParameter
import org.springframework.graphql.data.method.HandlerMethodArgumentResolver
import org.springframework.stereotype.Component

@Component
class UserIdArgumentGraphqlResolver(
    private val accessTokenService: AccessTokenService,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(UserId::class.java) && parameter.parameterType == Long::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        environment: DataFetchingEnvironment,
    ): Long {
        environment.graphQlContext.get<Long>(AuthGraphQlContext.USER_ID)?.let {
            return it
        }

        val authorization = environment.graphQlContext.get<String>(AuthGraphQlContext.AUTHORIZATION)
            ?: throw InvalidAuthTokenException("Authorization 헤더가 없습니다")

        val accessToken = BearerTokenExtractor.extract(authorization)

        val userId = accessTokenService.getUserId(accessToken)

        environment.graphQlContext.put(AuthGraphQlContext.USER_ID, userId)
        // 제거는 RequestLoggingFilter가 요청 종료 시 일괄 수행한다
        MDC.put(MdcKeys.USER_ID, userId.toString())
        return userId
    }

}
