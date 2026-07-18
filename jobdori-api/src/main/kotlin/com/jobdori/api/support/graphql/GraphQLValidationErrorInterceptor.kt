package com.jobdori.api.support.graphql

import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.logger.LoggerExtension.log
import graphql.GraphQLError
import graphql.validation.ValidationError
import org.springframework.graphql.execution.ErrorType
import org.springframework.graphql.server.WebGraphQlInterceptor
import org.springframework.graphql.server.WebGraphQlRequest
import org.springframework.graphql.server.WebGraphQlResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class GraphQLValidationErrorInterceptor : WebGraphQlInterceptor {

    override fun intercept(
        request: WebGraphQlRequest,
        chain: WebGraphQlInterceptor.Chain,
    ): Mono<WebGraphQlResponse> {
        return chain.next(request).map { response ->
            response.transform { builder ->
                builder.errors(response.executionResult.errors.map(::transformError))
            }
        }
    }

    internal fun transformError(error: GraphQLError): GraphQLError {
        if (error !is ValidationError) {
            return error
        }

        val errorCode = CommonErrorCode.E400_INVALID_ARGUMENTS
        val field = extractArgumentPath(error.message)

        log.warn { "[GRAPH_QL_VALIDATION] 잘못된 요청이 인입되었습니다. [type=${error.validationErrorType},field=${field ?: "unknown"}, locations=${error.locations}]" }

        return GraphQLError.newError()
            .errorType(ErrorType.BAD_REQUEST)
            .message(errorCode.message)
            .locations(error.locations)
            .extensions(buildMap {
                put("code", errorCode.code)
                field?.let {
                    put(
                        "details",
                        listOf(
                            mapOf(
                                "field" to it,
                                "reason" to INVALID_INPUT_FORMAT_REASON,
                            ),
                        ),
                    )
                }
            })
            .build()
    }

    private fun extractArgumentPath(message: String): String? {
        return ARGUMENT_PATH_REGEX.find(message)?.groupValues?.get(1)
    }

    private companion object {
        val ARGUMENT_PATH_REGEX = Regex("""argument '([^']+)'""")
        const val INVALID_INPUT_FORMAT_REASON = "입력 형식이 올바르지 않습니다."
    }

}
