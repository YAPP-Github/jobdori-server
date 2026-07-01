package com.jobdori.common.model

data class SliceResult<T>(
    val items: List<T>,
    val nextCursor: String?,
)
