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
        message = "Notion 연결에 실패했어요. Notion을 다시 연결해 주세요.",
        description = "요청한 Notion 연결이 없거나 접근 권한이 없는 경우",
    ),
    CONNECTION_NEED_RECONNECT(
        httpStatusCode = 409,
        code = "notion_connection_need_reconnect",
        message = "Notion 인증이 만료되었어요. Notion을 다시 연결해 주세요.",
        description = "Notion 토큰 갱신에 실패했거나 연결 권한이 해제된 경우",
    ),
    PAGE_ACCESS_DENIED(
        httpStatusCode = 403,
        code = "notion_page_access_denied",
        message = "Notion 페이지에 접근할 수 없어요. Notion에서 페이지 공유 설정을 확인해 주세요.",
        description = "해당 페이지가 연결에 공유되지 않았거나 권한이 없는 경우",
    ),
    API_REQUEST_FAILED(
        httpStatusCode = 502,
        code = "notion_api_request_failed",
        message = "Notion에서 내용을 가져오지 못했어요. 잠시 후 다시 시도해 주세요.",
        description = "Notion API 호출이 실패한 경우",
    ),
    ;

}
