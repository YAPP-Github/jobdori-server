package com.jobdori.core.domain.notion.error

import com.jobdori.common.error.ErrorCode

enum class NotionErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {

    CONNECTION_NOT_FOUND(
        httpStatusCode = 404,
        code = "notion_connection_not_found",
        message = "Notion 연결을 찾을 수 없습니다.",
        description = "요청한 Notion 연결이 없거나 접근 권한이 없는 경우",
    ),
    CONNECTION_NEED_RECONNECT(
        httpStatusCode = 409,
        code = "notion_connection_need_reconnect",
        message = "Notion을 다시 연결해야 합니다.",
        description = "Notion 토큰 갱신에 실패했거나 연결 권한이 해제된 경우",
    ),
    PAGE_ACCESS_DENIED(
        httpStatusCode = 403,
        code = "notion_page_access_denied",
        message = "Notion 페이지에 접근할 수 없습니다.",
        description = "해당 페이지가 연결에 공유되지 않았거나 권한이 없는 경우",
    ),
    API_REQUEST_FAILED(
        httpStatusCode = 502,
        code = "notion_api_request_failed",
        message = "Notion API 요청에 실패했습니다.",
        description = "Notion API 호출이 실패한 경우",
    ),
    ;

}
