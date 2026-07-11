package com.jobdori.api.application.notion.dto.request

import com.jobdori.api.application.common.dto.request.CursorRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Positive

data class ListNotionPageListRequest(
    override val cursor: String? = null,

    @field:Max(value = 30)
    @field:Positive
    override val size: Int = 10,
) : CursorRequest
