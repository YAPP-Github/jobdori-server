package com.jobdori.api.application.common.dto.response

data class CursorResponse(
    val nextCursor: String?,
) {

    val hasNext: Boolean = nextCursor != null

}
