package com.jobdori.api.libs

import com.jobdori.common.error.ErrorCode
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.snippet.TemplatedSnippet

class ErrorCodeSnippet(
    errorCodes: List<ErrorCodeEntry>,
) : TemplatedSnippet(
    "error-codes",
    mutableMapOf(
        "title" to "===== [Error Codes]\n",
        "fields" to errorCodes.map { entry ->
            mutableMapOf(
                "httpStatus" to entry.errorCode.httpStatusCode,
                "code" to entry.errorCode.code,
                "name" to entry.errorCode.name,
                "description" to (entry.description ?: entry.errorCode.description),
            )
        },
    )
) {

    override fun createModel(operation: Operation): Map<String, Any> = mutableMapOf()

    data class ErrorCodeEntry(
        val errorCode: ErrorCode,
        val description: String? = null,
    )

    companion object {
        fun errorCodeSnippet(
            vararg errorCodes: ErrorCode = arrayOf(),
        ): ErrorCodeSnippet {
            return ErrorCodeSnippet(errorCodes.map { ErrorCodeEntry(it) })
        }

        fun errorCodeSnippet(
            vararg errorCodes: Pair<ErrorCode, String>,
        ): ErrorCodeSnippet {
            return ErrorCodeSnippet(errorCodes.map { ErrorCodeEntry(it.first, it.second) })
        }
    }

}
