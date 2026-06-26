package com.jobdori.api.support.docs

import com.jobdori.api.DocsTest
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.error.ErrorCode
import com.jobdori.core.domain.user.error.UserErrorCode
import com.jobdori.core.domain.workspace.error.WorkspaceErrorCode
import io.kotest.core.spec.style.FunSpec
import java.io.File

@DocsTest
internal class ErrorCodeDocsGeneratorTest : FunSpec({

    test("REST 공통 에러코드 Asciidoctor 생성") {
        generateRestErrorCodeDocs(
            file = File("src/docs/asciidoc/restapi/common/error.adoc"),
            errorCodes = CommonErrorCode.entries,
        )
    }

    test("GraphQL 에러코드 Asciidoctor 생성") {
        generateGraphQlErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/common/error.adoc"),
            errorCodes = CommonErrorCode.entries,
        )
        generateGraphQlErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/user/user-error.adoc"),
            errorCodes = UserErrorCode.entries,
        )
        generateGraphQlErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/workspace/workspace-error.adoc"),
            errorCodes = WorkspaceErrorCode.entries,
        )
    }

})

private fun generateRestErrorCodeDocs(
    file: File,
    errorCodes: Iterable<ErrorCode>,
) {
    writeErrorCodeDocs(
        file = file,
        content = buildString {
            appendLine("[cols=\"10%,20%,30%,40%\", options=\"header\"]")
            appendLine("|===")
            appendLine("| Status Code | Error Code | Message | Description")
            appendLine()
            errorCodes.sorted()
                .forEach { errorCode ->
                    appendLine(
                        "| ${errorCode.httpStatusCode} | ${errorCode.code} | ${errorCode.message} | ${errorCode.description}"
                    )
                }
            append("|===")
        },
    )
}

private fun generateGraphQlErrorCodeDocs(
    file: File,
    errorCodes: Iterable<ErrorCode>,
) {
    writeErrorCodeDocs(
        file = file,
        content = buildString {
            appendLine("[.api-table]")
            appendLine("[cols=\"28%,36%,36%\",options=\"header\"]")
            appendLine("|===")
            appendLine("| Code | Message | Description")
            appendLine()
            errorCodes.sorted()
                .forEach { errorCode ->
                    appendLine("| ${errorCode.code}")
                    appendLine("| ${errorCode.message}")
                    appendLine("| ${errorCode.description}")
                    appendLine()
                }
            append("|===")
        },
    )
}

private fun writeErrorCodeDocs(
    file: File,
    content: String,
) {
    file.parentFile.mkdirs()
    if (!file.exists()) {
        file.createNewFile()
    }
    file.printWriter().use { out -> out.println(content) }
}

private fun Iterable<ErrorCode>.sorted(): List<ErrorCode> {
    return sortedWith(compareBy<ErrorCode> { it.httpStatusCode }.thenBy { it.code })
}
