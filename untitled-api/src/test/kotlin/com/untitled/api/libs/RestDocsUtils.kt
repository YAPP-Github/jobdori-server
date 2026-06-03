package com.untitled.api.libs

import org.springframework.restdocs.operation.OperationRequest
import org.springframework.restdocs.operation.OperationRequestFactory
import org.springframework.restdocs.operation.OperationResponse
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor
import org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor
import org.springframework.restdocs.snippet.Attributes
import java.net.URI
import java.util.EnumSet

object RestDocsUtils {

    private const val API_SERVLET_PATH = "/api"

    fun withApiServletPath(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return if (normalizedPath.startsWith(API_SERVLET_PATH)) {
            normalizedPath
        } else {
            API_SERVLET_PATH + normalizedPath
        }
    }

    fun getDocumentRequest(): OperationRequestPreprocessor {
        return preprocessRequest(
            modifyUris()
                .scheme("http")
                .host("localhost")
                .port(8000),
            apiServletPath(),
            prettyPrint(),
        )
    }

    private fun attribute(key: String, value: Any): Attributes.Attribute {
        return Attributes.Attribute(key, value)
    }

    fun remarks(value: Any): Attributes.Attribute {
        return attribute("remarks", value)
    }

    fun <T : Enum<T>> convertToString(enumClass: Class<T>): String {
        return EnumSet.allOf(enumClass).joinToString(separator = ", ") { it!!.name }
    }

    private fun apiServletPath(): OperationPreprocessor {
        return object : OperationPreprocessor {
            override fun preprocess(request: OperationRequest): OperationRequest = OperationRequestFactory().create(
                request.uri.withPath(withApiServletPath(request.uri.path)),
                request.method,
                request.content,
                request.headers,
                request.parts,
                request.cookies,
            )

            override fun preprocess(response: OperationResponse): OperationResponse {
                return response
            }
        }
    }

    private fun URI.withPath(path: String): URI {
        return URI(scheme, userInfo, host, port, path, query, fragment)
    }

}
