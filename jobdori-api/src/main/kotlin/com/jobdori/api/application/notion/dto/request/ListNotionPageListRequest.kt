package com.jobdori.api.application.notion.dto.request

import com.jobdori.api.application.common.dto.request.CursorRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Positive

data class ListNotionPageListRequest(
    override val cursor: String? = null,

    @field:Max(value = 30, message = "페이지당 조회 개수의 최댓값은 {value}입니다.")
    @field:Positive(message = "페이지당 조회 개수에 0보다 큰 값을 입력해 주세요.")
    override val size: Int = 10,
) : CursorRequest
