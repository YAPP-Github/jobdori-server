package com.jobdori.api.support.auth.graphql

import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthWebGraphQlInterceptor : WebGraphQlInterceptor {

    override fun intercept(
        request: WebGraphQlRequest,
        chain: WebGraphQlInterceptor.Chain,
    ): Mono<WebGraphQlResponse> {
        val authorization = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            ?: return chain.next(request)

        request.configureExecutionInput { _, builder ->
            builder.graphQLContext { context ->
                context.put(AuthGraphQlContext.AUTHORIZATION, authorization)
            }.build()
        }
        return chain.next(request)
    }

}
