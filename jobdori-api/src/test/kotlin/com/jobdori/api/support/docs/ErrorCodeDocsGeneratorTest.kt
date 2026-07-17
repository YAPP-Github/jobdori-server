package com.jobdori.api.support.docs

import com.jobdori.api.DocsTest
import com.jobdori.common.error.CommonErrorCode
import com.jobdori.common.error.ErrorCode
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.experience.error.ExperienceErrorCode
import com.jobdori.core.domain.experience.error.ExperienceProjectErrorCode
import com.jobdori.core.domain.jd.error.JdCrawlErrorCode
import com.jobdori.core.domain.jd.error.JdErrorCode
import com.jobdori.core.domain.notion.error.NotionErrorCode
import com.jobdori.core.domain.resume.error.ResumeErrorCode
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
        generateGraphQlErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/resume/resume-error.adoc"),
            errorCodes = ResumeErrorCode.entries,
        )
        generateGraphQlErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/jd/jd-error.adoc"),
            errorCodes = JdErrorCode.entries + JdCrawlErrorCode.entries,
        )
        generateGraphQlOperationErrorCodeDocs(
            file = File("src/docs/asciidoc/graphql/operation-error.adoc"),
            operationErrors = graphQlOperationErrors,
        )
        generateGraphQlOperationSampleDocs(
            file = File("src/docs/asciidoc/graphql/operation-sample.adoc"),
            operationErrors = graphQlOperationErrors,
        )
        generateGraphQlOperationDocs(
            file = File("src/docs/asciidoc/graphql/operation.adoc"),
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
        sampleFile = graphQlSample("user/me.graphql"),
        errorCodes = operationErrorCodes(
            UserErrorCode.E404_USER_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience",
        operation = "experience",
        title = "경험 단건 조회",
        type = GraphQlOperationType.QUERY,
        sampleFile = graphQlSample("experience/experience.graphql"),
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
        sampleFile = graphQlSample("experience/experiences.graphql"),
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
        sampleFile = graphQlSample("experience/search-experiences.graphql"),
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
        sampleFile = graphQlSample("experience/experience-project.graphql"),
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
        sampleFile = graphQlSample("experience/experience-projects.graphql"),
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
        sampleFile = graphQlSample("experience/create-experience.graphql"),
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
        sampleFile = graphQlSample("experience/update-experience.graphql"),
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
        sampleFile = graphQlSample("experience/delete-experience.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ExperienceErrorCode.E404_EXPERIENCE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Experience",
        operation = "polishExperienceContents",
        title = "경험 내용 다듬기",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("experience/polish-experience-contents.graphql"),
        errorCodes = emptyList(),
    ),
    GraphQlOperationError(
        category = "Experience Project",
        operation = "createExperienceProject",
        title = "경험 프로젝트 생성",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("experience/create-experience-project.graphql"),
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
        sampleFile = graphQlSample("experience/update-experience-project.graphql"),
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
        sampleFile = graphQlSample("experience/delete-experience-project.graphql"),
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
        sampleFile = graphQlSample("notion/connect-notion.graphql"),
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
        sampleFile = graphQlSample("notion/disconnect-notion.graphql"),
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
        sampleFile = graphQlSample("notion/notion-connections.graphql"),
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
        sampleFile = graphQlSample("notion/notion-pages.graphql"),
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
        title = "Notion 경험 불러오기",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("notion/import-notion-experiences.graphql"),
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
        category = "Resume",
        operation = "resumes",
        title = "이력서 목록 조회",
        type = GraphQlOperationType.QUERY,
        sampleFile = graphQlSample("resume/resumes.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Resume",
        operation = "resumeCounts",
        title = "이력서 상태별 개수 조회",
        type = GraphQlOperationType.QUERY,
        sampleFile = graphQlSample("resume/resume-counts.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Resume",
        operation = "resume",
        title = "이력서 상세 조회",
        type = GraphQlOperationType.QUERY,
        sampleFile = graphQlSample("resume/resume.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ResumeErrorCode.E404_RESUME_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Resume",
        operation = "createResume",
        title = "이력서 생성",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("resume/create-resume-from-profile.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Resume",
        operation = "updateResume",
        title = "이력서 수정",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("resume/update-resume.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ResumeErrorCode.E404_RESUME_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Resume",
        operation = "deleteResume",
        title = "이력서 삭제",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("resume/delete-resume.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            ResumeErrorCode.E404_RESUME_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "JD",
        operation = "jd",
        title = "JD 단건 조회",
        type = GraphQlOperationType.QUERY,
        sampleFile = graphQlSample("jd/jd.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            JdErrorCode.E404_JD_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "JD",
        operation = "jds",
        title = "JD 목록 조회",
        type = GraphQlOperationType.QUERY,
        sampleFile = graphQlSample("jd/jds.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "JD",
        operation = "registerJd",
        title = "JD 등록",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("jd/register-jd.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            JdCrawlErrorCode.E400_JD_INVALID_URL,
            JdCrawlErrorCode.E422_JD_ACCESS_DENIED,
            JdCrawlErrorCode.E422_JD_FETCH_FAILED,
            AiErrorCode.E429_AI_RATE_LIMITED,
            AiErrorCode.E500_AI_GENERATION_FAILED,
            AiErrorCode.E503_AI_UNAVAILABLE,
            AiErrorCode.E504_AI_TIMEOUT,
        ),
    ),
    GraphQlOperationError(
        category = "JD",
        operation = "markJdCompleted",
        title = "JD 완료 처리",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("jd/mark-jd-completed.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            JdErrorCode.E404_JD_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Profile",
        operation = "profile",
        title = "이력서 기본 정보 프로필 조회",
        type = GraphQlOperationType.QUERY,
        sampleFile = graphQlSample("profile/profile.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Profile",
        operation = "updateProfile",
        title = "이력서 기본 정보 프로필 수정",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("profile/update-profile.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
        ),
    ),
    GraphQlOperationError(
        category = "Profile",
        operation = "generateCoreCompetency",
        title = "핵심역량 AI 생성",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("profile/generate-core-competency.graphql"),
        errorCodes = operationErrorCodes(
            WorkspaceErrorCode.E403_WORKSPACE_ACCESS_DENIED,
            WorkspaceErrorCode.E404_WORKSPACE_NOT_FOUND,
            AiErrorCode.E500_AI_GENERATION_FAILED,
        ),
    ),
    GraphQlOperationError(
        category = "Profile",
        operation = "polishProfileText",
        title = "프로필 텍스트 AI 다듬기",
        type = GraphQlOperationType.MUTATION,
        sampleFile = graphQlSample("profile/polish-profile-text.graphql"),
        errorCodes = operationErrorCodes(
            AiErrorCode.E500_AI_GENERATION_FAILED,
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
            operationErrors.forEach { operationError ->
                appendLine("// tag::${operationError.operation}[]")
                if (operationError.errorCodes.isEmpty()) {
                    appendLine("커스텀 에러 코드: 없음")
                    appendLine()
                    appendLine("전역 공통 에러 코드는 <<graphql-common-error-codes,공통 에러 코드>>를 참고해 주세요.")
                } else {
                    appendLine("커스텀 에러 코드:")
                    appendLine()
                    appendLine("전역 공통 에러 코드는 <<graphql-common-error-codes,공통 에러 코드>>를 참고해 주세요. +")
                    appendLine("아래에는 각 GraphQL operation의 요청 샘플과 도메인 로직에 따라 발생할 수 있는 커스텀 에러 코드만 명시합니다.")
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
                }
                appendLine("// end::${operationError.operation}[]")
                appendLine()
            }
        },
    )
}

private fun generateGraphQlOperationSampleDocs(
    file: File,
    operationErrors: List<GraphQlOperationError>,
) {
    writeErrorCodeDocs(
        file = file,
        content = buildString {
            operationErrors.forEach { operationError ->
                appendLine("// tag::${operationError.operation}[]")
                appendLine("[.graphql-sample]")
                appendLine("====")
                appendLine("요청 샘플:")
                appendLine()
                appendLine("[source,graphql]")
                appendLine("----")
                appendLine(operationError.sampleFile.readText().trimEnd())
                appendLine("----")
                appendLine("====")
                appendLine("// end::${operationError.operation}[]")
                appendLine()
            }
        },
    )
}

private fun generateGraphQlOperationDocs(
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
                        appendLine("include::./operation-sample.adoc[tag=${operationError.operation}]")
                        appendLine()
                        appendLine("include::./operation-error.adoc[tag=${operationError.operation}]")
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
    file.printWriter().use { out ->
        out.println("// 이 파일은 ErrorCodeDocsGeneratorTest에서 생성됩니다. 직접 수정하지 마세요.")
        out.println()
        out.println(content)
    }
}

private fun Iterable<ErrorCode>.sorted(): List<ErrorCode> {
    return sortedWith(compareBy<ErrorCode> { it.httpStatusCode }.thenBy { it.code })
}

private data class GraphQlOperationError(
    val category: String,
    val operation: String,
    val title: String,
    val type: GraphQlOperationType,
    val sampleFile: File,
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

private fun graphQlSample(path: String): File {
    return File("http/graphql/$path")
}
