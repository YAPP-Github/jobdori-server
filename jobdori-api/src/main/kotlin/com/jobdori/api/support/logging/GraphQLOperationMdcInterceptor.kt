package com.jobdori.api.support.logging

import org.slf4j.MDC
import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * GraphQL 요청은 전부 단일 경로(POST /api/graphql)로 들어와 http.url만으로는 구분되지 않는다.
 * 오퍼레이션명을 MDC에 넣어야 오퍼레이션별 에러율/지연을 볼 수 있다.
 */
@Component
class GraphQLOperationMdcInterceptor : WebGraphQlInterceptor {

    override fun intercept(
        request: WebGraphQlRequest,
        chain: WebGraphQlInterceptor.Chain,
    ): Mono<WebGraphQlResponse> {
        val operationName = request.operationName ?: return chain.next(request)

        // 제거는 RequestLoggingFilter가 요청 종료 시 일괄 수행한다.
        // 여기서 지우면 필터가 남기는 액세스 로그에 오퍼레이션명이 빠진다.
        MDC.put(MdcKeys.GRAPHQL_OPERATION, operationName)
        return chain.next(request)
    }

}
