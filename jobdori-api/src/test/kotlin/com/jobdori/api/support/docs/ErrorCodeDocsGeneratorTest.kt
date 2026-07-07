package com.jobdori.api.support.docs

import com.jobdori.api.DocsTest
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.error.ErrorCode
import com.jobdori.core.domain.experience.error.ExperienceErrorCode
import com.jobdori.core.domain.experience.error.ExperienceProjectErrorCode
import com.jobdori.core.domain.notion.error.NotionErrorCode
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
        generateGraphQlErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/experience/experience-error.adoc"),
            errorCodes = ExperienceErrorCode.entries + ExperienceProjectErrorCode.entries,
        )
        generateGraphQlErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/notion/notion-error.adoc"),
            errorCodes = NotionErrorCode.entries,
        )
        generateGraphQlOperationErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/operation-error.adoc"),
            operationErrors = graphQlOperationErrors,
        )
    }

})

private val graphQlOperationErrors = listOf(
    GraphQlOperationError(
        category = "User",
        operation = "me",
        title = "내 정보 조회",
        type = GraphQlOperationType.QUERY,
        errorCodes = operationErrorCodes(
            UserErrorCode.E404_USER_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience",
        operation = "experience",
        title = "경험 단건 조회",
        type = GraphQlOperationType.QUERY,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceErrorCode.E404_EXPERIENCE_NOT_FOUND,
            ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND to "`project` 필드를 함께 요청했으나 연결된 경험 프로젝트를 찾을 수 없는 경우",
        ),
    ),
    GraphQlOperationError(
        category = "Experience",
        operation = "experiences",
        title = "경험 목록 조회",
        type = GraphQlOperationType.QUERY,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND to "`projectId`로 특정 프로젝트의 경험 목록을 조회했으나 프로젝트를 찾을 수 없는 경우",
        ),
    ),
    GraphQlOperationError(
        category = "Experience",
        operation = "searchExperiences",
        title = "경험 검색",
        type = GraphQlOperationType.QUERY,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience Project",
        operation = "experienceProject",
        title = "경험 프로젝트 단건 조회",
        type = GraphQlOperationType.QUERY,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience Project",
        operation = "experienceProjects",
        title = "경험 프로젝트 목록 조회",
        type = GraphQlOperationType.QUERY,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience",
        operation = "createExperience",
        title = "경험 생성",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience",
        operation = "updateExperience",
        title = "경험 수정",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceErrorCode.E404_EXPERIENCE_NOT_FOUND,
            ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience",
        operation = "deleteExperience",
        title = "경험 삭제",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceErrorCode.E404_EXPERIENCE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience Project",
        operation = "createExperienceProject",
        title = "경험 프로젝트 생성",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience Project",
        operation = "updateExperienceProject",
        title = "경험 프로젝트 수정",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience Project",
        operation = "deleteExperienceProject",
        title = "경험 프로젝트 삭제",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Notion",
        operation = "connectNotion",
        title = "Notion 연결",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            NotionErrorCode.API_REQUEST_FAILED to "Notion OAuth 토큰 교환 요청이 실패한 경우",
        ),
    ),
    GraphQlOperationError(
        category = "Notion",
        operation = "disconnectNotion",
        title = "Notion 연결 해제",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Notion",
        operation = "notionConnections",
        title = "Notion 연결 목록 조회",
        type = GraphQlOperationType.QUERY,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Notion",
        operation = "notionPages",
        title = "Notion 페이지 검색",
        type = GraphQlOperationType.QUERY,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            NotionErrorCode.CONNECTION_NOT_FOUND,
            NotionErrorCode.CONNECTION_NEED_RECONNECT,
            NotionErrorCode.PAGE_ACCESS_DENIED,
            NotionErrorCode.API_REQUEST_FAILED,
        ),
    ),
    GraphQlOperationError(
        category = "Notion",
        operation = "importNotionExperiences",
        title = "Notion 경험 가져오기",
        type = GraphQlOperationType.MUTATION,
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            NotionErrorCode.CONNECTION_NOT_FOUND,
            NotionErrorCode.CONNECTION_NEED_RECONNECT,
            NotionErrorCode.PAGE_ACCESS_DENIED,
            NotionErrorCode.API_REQUEST_FAILED,
        ),
    ),
)

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

private fun generateGraphQlOperationErrorCodeDocs(
    file: File,
    operationErrors: List<GraphQlOperationError>,
) {
    writeErrorCodeDocs(
        file = file,
        content = buildString {
            operationErrors.groupBy { it.category }
                .forEach { (category, categoryOperationErrors) ->
                    appendLine("=== $category")
                    appendLine()
                    categoryOperationErrors.forEach { operationError ->
                        appendLine("==== ${operationError.title}")
                        appendLine()
                        appendLine("Type: `${operationError.type.label}`")
                        appendLine()
                        appendLine("Operation: `${operationError.operation}`")
                        appendLine()
                        appendLine("[.api-table]")
                        appendLine("[cols=\"28%,32%,40%\",options=\"header\"]")
                        appendLine("|===")
                        appendLine("| Code | Message | Description")
                        appendLine()
                        operationError.errorCodes.forEach { entry ->
                            appendLine("| ${entry.errorCode.code}")
                            appendLine("| ${entry.errorCode.message}")
                            appendLine("| ${entry.description}")
                            appendLine()
                        }
                        appendLine("|===")
                        appendLine()
                    }
                }
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

private data class GraphQlOperationError(
    val category: String,
    val operation: String,
    val title: String,
    val type: GraphQlOperationType,
    val errorCodes: List<GraphQlOperationErrorCode>,
)

private enum class GraphQlOperationType(
    val label: String,
) {
    QUERY("Query"),
    MUTATION("Mutation"),
}

private data class GraphQlOperationErrorCode(
    val errorCode: ErrorCode,
    val description: String = errorCode.description,
)

private fun operationErrorCodes(vararg errorCodes: Any): List<GraphQlOperationErrorCode> {
    return errorCodes.map { errorCode ->
        when (errorCode) {
            is ErrorCode -> GraphQlOperationErrorCode(errorCode)
            is GraphQlOperationErrorCode -> errorCode
            else -> error("Unsupported GraphQL operation error code: $errorCode")
        }
    }
}

private infix fun ErrorCode.to(description: String): GraphQlOperationErrorCode {
    return GraphQlOperationErrorCode(
        errorCode = this,
        description = description,
    )
}
